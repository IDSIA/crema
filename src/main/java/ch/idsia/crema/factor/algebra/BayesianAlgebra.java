package ch.idsia.crema.factor.algebra;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.bayesian.BayesianOperation;
import ch.idsia.crema.factor.algebra.bayesian.LogBayesianOperation;
import ch.idsia.crema.factor.algebra.bayesian.SimpleBayesianOperation;
import ch.idsia.crema.factor.algebra.collectors.Collector;
import ch.idsia.crema.factor.algebra.collectors.Filter;
import ch.idsia.crema.factor.algebra.collectors.LogMarginal;
import ch.idsia.crema.factor.algebra.collectors.Marginal;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 14:40
 */
public class BayesianAlgebra implements Operation<BayesianFactor> {

	protected BayesianFactor collect(final BayesianFactor factor, final int offset, final Collector collector) {
		final Strides domain = factor.getDomain();

		final int[] new_variables = new int[domain.getSize() - 1];
		final int[] new_sizes = new int[domain.getSize() - 1];

		System.arraycopy(domain.getVariables(), 0, new_variables, 0, offset);
		System.arraycopy(domain.getVariables(), offset + 1, new_variables, offset, new_variables.length - offset);

		System.arraycopy(domain.getSizes(), 0, new_sizes, 0, offset);
		System.arraycopy(domain.getSizes(), offset + 1, new_sizes, offset, new_variables.length - offset);

		final int stride = domain.getStrideAt(offset);
		final int size = domain.getSizeAt(offset);
		final int reset = size * stride;

		int source = 0;
		int next = stride;
		int jump = stride * (size - 1);

		Strides target_domain = new Strides(new_variables, new_sizes);
		final double[] new_data = new double[target_domain.getCombinations()];

		for (int target = 0; target < target_domain.getCombinations(); ++target, ++source) {
			if (source == next) {
				source += jump;
				next += reset;
			}

			new_data[target] = collector.collect(factor.getData(), source);
		}

		if (factor.isLog())
			return new BayesianLogFactor(target_domain, new_data);
		else
			return new BayesianDefaultFactor(target_domain, new_data);
	}

	/**
	 * The specialized method that avoids the cast of the input variable.
	 *
	 * <p>
	 * This implementation uses long values for strides, sizes and indices,
	 * allowing for combined operations. Increasing the index is, for instance,
	 * one single add instead of two. The values will contain in the Lower 32bit
	 * this factor's values and in the upper 32 the parameter's ones. Given that
	 * most architectures are 64 bit, this should give a tiny performance
	 * improvement.
	 * </p>
	 *
	 * @param one
	 * @param two
	 * @return
	 */
	@Override
	public BayesianFactor combine(BayesianFactor one, BayesianFactor two) {
		final boolean oneIsLog = one.isLog();
		final boolean twoIsLog = two.isLog();

		final BayesianOperation ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleBayesianOperation();
		} else {
			ops = new LogBayesianOperation();

			if (!oneIsLog)
				one = new BayesianLogFactor((BayesianDefaultFactor) one);
			if (!two.isLog())
				two = new BayesianLogFactor((BayesianDefaultFactor) two);
		}

		final Strides target = one.getDomain().union(two.getDomain());
		final int length = target.getSize();

		final int[] limits = new int[length];
		final int[] assign = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < one.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), one.getDomain().getVariables()[vindex]);
			stride[offset] = one.getDomain().getStrides()[vindex];
		}

		for (int vindex = 0; vindex < two.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), two.getDomain().getVariables()[vindex]);
			stride[offset] += ((long) two.getDomain().getStrides()[vindex] << 32L);
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = target.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		long idx = 0;
		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			final int idx1 = (int) (idx & 0xFFFFFFFF);
			final int idx2 = (int) (idx >>> 32L);

			result[i] = ops.combine(one, idx1, two, idx2);

			for (int l = 0; l < length; ++l) {
				if (assign[l] == limits[l]) {
					assign[l] = 0;
					idx -= reset[l];
				} else {
					++assign[l];
					idx += stride[l];
					break;
				}
			}
		}

		if (oneIsLog && twoIsLog)
			return new BayesianLogFactor(target, result);
		else
			return new BayesianDefaultFactor(target, result);
	}

	/**
	 * Reduce the domain by removing a variable and selecting the specified
	 * state.
	 *
	 * @param variable the variable to be filtered out
	 * @param state    the state to be selected
	 */
	@Override
	public BayesianFactor filter(BayesianFactor factor, int variable, int state) {
		final Strides domain = factor.getDomain();
		final int offset = domain.indexOf(variable);
		return collect(factor, offset, new Filter(domain.getStrideAt(offset), state));
	}

	/**
	 * <p>
	 * Marginalize a variable out of the factor. This corresponds to sum all
	 * parameters that differ only in the state of the marginalized variable.
	 * </p>
	 *
	 * <p>
	 * If this factor represent a Conditional Probability Table you should only
	 * marginalize variables on the right side of the conditioning bar. If so,
	 * there is no need for further normalization.
	 * </p>
	 *
	 * @param variable the variable to be summed out of the CPT
	 * @return the new CPT with the variable marginalized out.
	 */
	@Override
	public BayesianFactor marginalize(BayesianFactor factor, int variable) {
		final Strides domain = factor.getDomain();
		final int offset = domain.indexOf(variable);

		if (factor.isLog())
			return collect(factor, offset, new LogMarginal(domain.getSizeAt(offset), domain.getStrideAt(offset)));
		else
			return collect(factor, offset, new Marginal(domain.getSizeAt(offset), domain.getStrideAt(offset)));
	}

	/**
	 * divide this factor by the provided one. This assumes that the domain of
	 * the given factor is a subset of this one's.
	 *
	 * @param one
	 * @param two
	 * @return
	 */
	public BayesianFactor divide(BayesianFactor one, BayesianFactor two) {
		final boolean oneIsLog = one.isLog();
		final boolean twoIsLog = two.isLog();

		final BayesianOperation ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleBayesianOperation();
		} else {
			ops = new LogBayesianOperation();

			if (!oneIsLog)
				one = new BayesianLogFactor((BayesianDefaultFactor) one);
			if (!two.isLog())
				two = new BayesianLogFactor((BayesianDefaultFactor) two);
		}

		final Strides target = one.getDomain();
		final int length = target.getSize();

		final int[] limits = new int[length];
		final int[] assign = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < length; ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), one.getDomain().getVariables()[vindex]);
			stride[offset] = one.getDomain().getStrides()[vindex];
		}

		for (int vindex = 0; vindex < two.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), two.getDomain().getVariables()[vindex]);
			stride[offset] += ((long) two.getDomain().getStrides()[vindex] << 32L);
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = target.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		long idx = 0;
		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			final int idx1 = (int) (idx & 0xFFFFFFFF);
			final int idx2 = (int) (idx >>> 32L);

			result[i] = ops.divide(one, idx1, two, idx2);

			for (int l = 0; l < length; ++l) {
				if (assign[l] == limits[l]) {
					assign[l] = 0;
					idx -= reset[l];
				} else {
					++assign[l];
					idx += stride[l];
					break;
				}
			}
		}

		if (oneIsLog && twoIsLog)
			return new BayesianLogFactor(target, result);
		else
			return new BayesianDefaultFactor(target, result);
	}

	/**
	 * Factor normalization.
	 */
	public BayesianFactor normalize(final BayesianFactor factor, final int... given) {
		BayesianFactor div = factor;
		for (int m : ArraysUtil.removeAllFromSortedArray(factor.getDomain().getVariables(), given)) {
			div = marginalize(div, m);
		}

		return divide(factor, div);
	}

}