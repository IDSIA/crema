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

public abstract class IntervalAbstractFactor implements IntervalFactor {

	protected Strides dataDomain;
	protected Strides groupDomain;

	public IntervalAbstractFactor(Strides content, Strides separation) {
		setConditioningDomain(content);
		setDataDomain(separation);
	}

	public void setDataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
	}

	public void setConditioningDomain(Strides groupDomain) {
		if (groupDomain == null) {
			groupDomain = new Strides(new int[0], new int[0]);
		}
		this.groupDomain = groupDomain;
	}

	@Override
	public Strides getDataDomain() {
		return dataDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return groupDomain;
	}

	@Override
	public Strides getDomain() {
		return dataDomain.union(groupDomain);
	}

	protected double upperBound() {
		return 1.0;
	}

	protected double lowerBound() {
		return 0.0;
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

	@Override
	public double[][] getDataLower() {
		return lower;
	}

	@Override
	public double[][] getDataUpper() {
		return upper;
	}

	/**
	 * Merges the bounds with another interval factor
	 *
	 * @param f
	 * @return
	 */
	@Override
	public IntervalFactor merge(IntervalFactor f) {
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
