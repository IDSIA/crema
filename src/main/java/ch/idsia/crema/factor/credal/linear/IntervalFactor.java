package ch.idsia.crema.factor.credal.linear;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class IntervalFactor extends SeparateFactor<IntervalFactor> implements SeparateLinearFactor<IntervalFactor> {
	private final double[][] lower;
	private final double[][] upper;

	public IntervalFactor(Strides content, Strides separation) {
		super(content, separation);

		this.lower = new double[groupDomain.getCombinations()][content.getCombinations()];
		this.upper = new double[groupDomain.getCombinations()][content.getCombinations()];

		// init all to 0-1
		for (int i = 0; i < groupDomain.getCombinations(); ++i) {
			Arrays.fill(this.upper[i], 1.0);
			//Arrays.fill(this.lower[i], 0.0); // default is 0 anyway
		}
	}

	public IntervalFactor(Strides content, Strides separation, double[][] lower, double[][] upper) {
		super(content, separation);

		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public IntervalFactor copy() {
		double[][] new_lower = new double[groupDomain.getCombinations()][];
		double[][] new_upper = new double[groupDomain.getCombinations()][];

		for (int i = 0; i < groupDomain.getCombinations(); ++i) {
			new_lower[i] = this.lower[i].clone();
			new_upper[i] = this.upper[i].clone();
		}

		return new IntervalFactor(dataDomain, groupDomain, new_lower, new_upper);
	}

	/**
	 * Set the lower and upper bounds for a specific conditioning (grouping)
	 *
	 * @param lowers
	 * @param uppers
	 * @param states
	 */
	public void set(double[] lowers, double[] uppers, int... states) {
		int offset = groupDomain.getOffset(states);
		this.lower[offset] = lowers;
		this.upper[offset] = uppers;
	}

	public double[] getLower(int... states) {
		return this.lower[groupDomain.getOffset(states)];
	}

	public double[] getUpper(int... states) {
		return this.upper[groupDomain.getOffset(states)];
	}

	public double[] getLowerAt(int group_offset) {
		return this.lower[group_offset];
	}

	public double[] getUpperAt(int group_offset) {
		return this.upper[group_offset];
	}

	/**
	 * Array is NOT copied
	 *
	 * @param value
	 * @param states
	 */
	public void setLower(double[] value, int... states) {
		int offset = groupDomain.getOffset(states);
		this.lower[offset] = value;
	}

	public void setUpper(double[] value, int... states) {
		int offset = groupDomain.getOffset(states);
		this.upper[offset] = value;
	}

	protected double defaultUpperBound() {
		return 1.0;
	}

	protected double defaultLowerBound() {
		return 0.0;
	}

	/**
	 * <p>Sets the lower and upper values for the element at specified offset
	 * and group.</p>
	 *
	 * <p>This is particularly useful for factor whose data domain
	 * is a single variable and the group domain represents the conditioning.
	 * In such cases the offset is the state and this method can be used to
	 * set the state's bounds given a parent configuration.
	 * </p>
	 *
	 * @param lower
	 * @param upper
	 * @param dataOffset
	 * @param given
	 */
	public void setBounds(double lower, double upper, int dataOffset, int... given) {
		int offset = groupDomain.getOffset(given);
		this.lower[offset][dataOffset] = lower;
		this.upper[offset][dataOffset] = upper;
	}

	@Override
	public IntervalFactor filter(int variable, int state) {
		int var_offset = groupDomain.indexOf(variable);
		int var_stride = groupDomain.getStrideAt(var_offset);

		int next_stride = groupDomain.getStrideAt(var_offset + 1);

		int state_offset = var_stride * state;
		int block_count = next_stride / var_stride;

		Strides new_domain = groupDomain.removeAt(var_offset);
		int new_size = new_domain.getCombinations();
		double[][] new_lower = new double[new_size][];
		double[][] new_upper = new double[new_size][];

		for (int i = 0; i < block_count; ++i) {
			System.arraycopy(lower, i * next_stride + state_offset, new_lower, i * var_stride, var_stride);
			System.arraycopy(upper, i * next_stride + state_offset, new_upper, i * var_stride, var_stride);
		}

		return new IntervalFactor(dataDomain, new_domain, new_lower, new_upper);
	}

	@Override
	public LinearConstraintSet getLinearProblem(int... states) {
		int offset = groupDomain.getOffset(states);
		return getLinearProblemAt(offset);
	}

	@Override
	public LinearConstraintSet getLinearProblemAt(int offset) {
		ArrayList<LinearConstraint> constraints = new ArrayList<>();
		double[] low = lower[offset];
		double[] hig = upper[offset];
		int length = dataDomain.getCombinations();

		for (int i = 0; i < length; ++i) {
			RealVector coeff = new OpenMapRealVector(length);
			coeff.setEntry(i, 1);

			constraints.add(new LinearConstraint(coeff, Relationship.GEQ, low[i]));
			constraints.add(new LinearConstraint(coeff, Relationship.LEQ, hig[i]));
		}

		// and we need a constraint telling us that the sum of the states must be one
		double[] one = new double[length];
		for (int i = 0; i < length; ++i) one[i] = 1;
		constraints.add(new LinearConstraint(one, Relationship.EQ, 1));

		return new LinearConstraintSet(constraints);
	}

	public boolean updateReachability() {
		for (int group = 0; group < groupDomain.getCombinations(); ++group) {

			for (int s = 0; s < dataDomain.getCombinations(); ++s) {
				double up = -upper[group][s];
				double lo = -lower[group][s];

				for (int s2 = 0; s2 < dataDomain.getCombinations(); ++s2) {
					up += upper[group][s2];
					lo += lower[group][s2];
				}

				upper[group][s] = Math.min(upper[group][s], 1 - lo);
				lower[group][s] = Math.max(lower[group][s], 1 - up);
				if (upper[group][s] < lower[group][s]) return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
	   StringBuilder sb = new StringBuilder();
	   sb.append("P(")
			 .append(Arrays.toString(getDataDomain().getVariables()))
			 .append(" | ")
			 .append(Arrays.toString(getSeparatingDomain().getVariables()))
			 .append(")");
	   sb.append("\n\t");
	   for (double[] x : lower)
		  sb.append(Arrays.toString(x));
	   sb.append("\n\t");
	   for (double[] x : upper)
		  sb.append(Arrays.toString(x));
	   return sb.toString();
	}

	public double[][] getDataLower() {
		return lower;
	}

	public double[][] getDataUpper() {
		return upper;
	}

	/**
	 * Merges the bounds with another interval factor
	 *
	 * @param f
	 * @return
	 */
	private IntervalFactor merge(IntervalFactor f) {
		if (!ArraysUtil.equals(this.getDomain().getVariables(), f.getDomain().getVariables(), true, true))
			throw new IllegalArgumentException("Inconsistent domains");

		double[][] lbounds = Stream.of(this.getDataLower()).map(double[]::clone).toArray(double[][]::new);
		double[][] ubounds = Stream.of(this.getDataUpper()).map(double[]::clone).toArray(double[][]::new);

		for (int i = 0; i < f.getSeparatingDomain().getCombinations(); i++) {
			for (int j = 0; j < f.getDataDomain().getCombinations(); j++) {
				lbounds[i][j] = Math.min(lbounds[i][j], f.getDataLower()[i][j]);
				ubounds[i][j] = Math.max(ubounds[i][j], f.getDataUpper()[i][j]);
			}
		}

		return new IntervalFactor(f.getDataDomain(), f.getSeparatingDomain(), lbounds, ubounds);
	}

	/**
	 * Merges the bounds with other interval factors
	 *
	 * @param factors
	 * @return
	 */
	public IntervalFactor merge(IntervalFactor... factors) {
		if (factors.length == 1)
			return merge(factors[0]);

		IntervalFactor out = this;

		for (IntervalFactor f : factors)
			out = out.merge(f);

		return out;
	}

	public static IntervalFactor mergeBounds(IntervalFactor... factors) {
		return factors[0].merge(factors);
	}

	public boolean isInside(BayesianFactor f) {
		IndexIterator it = f.getDomain().getReorderedIterator(this.getDomain().getVariables());

		double[] lbounds = Doubles.concat(this.getDataLower());
		double[] ubounds = Doubles.concat(this.getDataUpper());

		for (int i = 0; i < lbounds.length; i++) {
			int j = it.next();
			double pl = lbounds[i];
			double pu = ubounds[i];
			double q = f.getValueAt(j);

			if (q < pl || q > pu)
				return false;
		}
		return true;
	}
}
