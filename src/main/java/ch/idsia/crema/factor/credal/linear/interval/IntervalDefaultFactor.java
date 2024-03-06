package ch.idsia.crema.factor.credal.linear.interval;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.ArraysMath;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import com.google.common.primitives.Doubles;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.04.2021 18:50
 */
public class IntervalDefaultFactor extends IntervalAbstractFactor {

	protected final double[][] lower;
	protected final double[][] upper;

	protected IntervalDefaultFactor(Strides content, Strides separation) {
		super(content, separation);
		this.lower = new double[groupDomain.getCombinations()][content.getCombinations()];
		this.upper = new double[groupDomain.getCombinations()][content.getCombinations()];
	}

	public IntervalDefaultFactor(Strides content, Strides separation, double[][] lower, double[][] upper) {
		super(content, separation);
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public IntervalDefaultFactor copy() {
		double[][] new_lower = ArraysUtil.deepClone(lower);
		double[][] new_upper = ArraysUtil.deepClone(upper);
		return new IntervalDefaultFactor(dataDomain, groupDomain, new_lower, new_upper);
	}

	@Override
	public double[] getLower(int... states) {
		return lower[groupDomain.getOffset(states)];
	}

	@Override
	public double[] getUpper(int... states) {
		return upper[groupDomain.getOffset(states)];
	}

	@Override
	public double[] getLowerAt(int group_offset) {
		return lower[group_offset];
	}

	@Override
	public double[] getUpperAt(int group_offset) {
		return upper[group_offset];
	}

	@Override
	public double[] getLogLower(int... states) {
		return ArraysMath.log(getLower(states));
	}

	@Override
	public double[] getLogUpper(int... states) {
		return ArraysMath.log(getUpper(states));
	}

	@Override
	public double[] getLogLowerAt(int group_offset) {
		return ArraysMath.log(getLowerAt(group_offset));
	}

	@Override
	public double[] getLogUpperAt(int group_offset) {
		return ArraysMath.log(getUpperAt(group_offset));
	}

	@Override
	public IntervalDefaultFactor filter(int variable, int state) {
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

		return new IntervalDefaultFactor(dataDomain, new_domain, new_lower, new_upper);
	}

	@Override
	public IntervalDefaultFactor updateReachability() {
		double[][] newUpper = new double[upper.length][];
		double[][] newLower = new double[lower.length][];

		final int n = dataDomain.getCombinations();

		for (int group = 0; group < groupDomain.getCombinations(); ++group) {

			newUpper[group] = new double[n];
			newLower[group] = new double[n];

			for (int s = 0; s < n; ++s) {
				double up = -upper[group][s];
				double lo = -lower[group][s];

				for (int s2 = 0; s2 < n; ++s2) {
					up += upper[group][s2];
					lo += lower[group][s2];
				}

				newUpper[group][s] = Math.min(upper[group][s], 1 - lo);
				newLower[group][s] = Math.max(lower[group][s], 1 - up);
				if (upper[group][s] < lower[group][s]) {
					// TODO: is this an error?
					System.err.printf("WARNING: inconsistent interval found for group %d value %d: upper is %f lower is %f %n", group, s, upper[group][s], lower[group][s]);
				}
			}
		}

		return new IntervalDefaultFactor(dataDomain, groupDomain, newLower, newUpper);
	}

	@Override
	public boolean isInside(BayesianFactor f) {
		IndexIterator it = f.getDomain().getReorderedIterator(getDomain().getVariables());

		double[] lbounds = Doubles.concat(lower);
		double[] ubounds = Doubles.concat(upper);

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

	/**
	 * Merges the bounds with another interval factor.
	 *
	 * @param factor other factor to merge with
	 * @return a new {@link IntervalDefaultFactor}, result of merging this factor with the given factor
	 * @throws IllegalArgumentException if the number of variables in the domains of this and the given factor is different
	 */
	@Override
	public IntervalDefaultFactor merge(IntervalFactor factor) {
		if (!ArraysUtil.equals(this.getDomain().getVariables(), factor.getDomain().getVariables(), true, true))
			throw new IllegalArgumentException("Inconsistent domains");

		final IntervalDefaultFactor other = (IntervalDefaultFactor) factor;

		double[][] lbounds = ArraysUtil.deepClone(lower);
		double[][] ubounds = ArraysUtil.deepClone(upper);

		for (int i = 0; i < other.getSeparatingDomain().getCombinations(); i++) {
			for (int j = 0; j < other.getDataDomain().getCombinations(); j++) {
				lbounds[i][j] = Math.min(lbounds[i][j], other.lower[i][j]);
				ubounds[i][j] = Math.max(ubounds[i][j], other.upper[i][j]);
			}
		}

		return new IntervalDefaultFactor(other.getDataDomain(), other.getSeparatingDomain(), lbounds, ubounds);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("\n\t");
		for (double[] x : lower)
			sb.append(Arrays.toString(x));
		sb.append("\n\t");
		for (double[] x : upper)
			sb.append(Arrays.toString(x));
		return sb.toString();
	}

}
