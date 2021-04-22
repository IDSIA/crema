package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.GenericOperationFunction;
import ch.idsia.crema.factor.algebra.bayesian.*;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 18:43
 */
public abstract class BayesianAbstractFactor implements BayesianFactor {

	protected Strides domain;

	public BayesianAbstractFactor(Domain domain) {
		this.domain = Strides.fromDomain(domain);
	}

	@Override
	public Strides getDomain() {
		return domain;
	}

	@Override
	public Strides getSeparatingDomain() {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public Strides getDataDomain() {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public double getValue(int... states) {
		return getValueAt(domain.getOffset(states));
	}

	@Override
	public double getLogValue(int... states) {
		return getLogValueAt(domain.getOffset(states));
	}

	protected <F extends BayesianAbstractFactor> F collect(int offset, BayesianFactorBuilder<F> builder, BayesianCollector collector) {
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

			new_data[target] = collector.collect(this, source);
		}

		return builder.get(target_domain, new_data);
	}

	/**
	 * <p>
	 * This implementation uses long values for strides, sizes and indices,
	 * allowing for combined operations. Increasing the index is, for instance,
	 * one single add instead of two. The values will contain in the Lower 32bit
	 * this factor's values and in the upper 32 the parameter's ones. Given that
	 * most architectures are 64 bit, this should give a tiny performance
	 * improvement.
	 * </p>
	 *
	 * @param factor  other factor to combine with
	 * @param builder supplier that can build a new BayesianFactor from domain and data
	 * @param op      operations to use
	 * @param <F>     returned type
	 * @return a factor, combination of this factor with the given other factor
	 */
	@SuppressWarnings("unchecked")
	protected <F extends BayesianAbstractFactor> F combine(F factor, BayesianFactorBuilder<F> builder, GenericOperationFunction<F> op) {
		// domains should be sorted
		this.sortDomain();
		factor = (F) factor.copy();
		factor.sortDomain();

		final Strides target = getDomain().union(factor.getDomain());
		final int length = target.getSize();

		final int[] limits = new int[length];
		final int[] assign = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), getDomain().getVariables()[vindex]);
			stride[offset] = getDomain().getStrides()[vindex];
		}

		for (int vindex = 0; vindex < factor.getDomain().getSize(); ++vindex) {
			int offset = ArraysUtil.indexOf(factor.getDomain().getVariables()[vindex], target.getVariables());
			stride[offset] += ((long) factor.getDomain().getStrides()[vindex] << 32L);
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = target.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		long idx = 0;
		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			result[i] = op.f((F) this, (int) (idx & 0xFFFFFFFF), factor, (int) (idx >>> 32L));

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

		return builder.get(target, result);
	}

	/**
	 * Divides this factor by the provided one.
	 * <p>
	 * This assumes that the domain of the given factor is a subset of this one's.
	 *
	 * @param factor  other factor to combine with
	 * @param builder Supplier that can build a new BayesianFactor from domain and data
	 * @param op      Operation to apply
	 * @param <F>     returned type
	 * @return a factor, combination of this factor with the given other factor
	 */
	@SuppressWarnings("unchecked")
	protected <F extends BayesianFactor> F divide(F factor, BayesianFactorBuilder<F> builder, GenericOperationFunction<F> op) {
		final int length = domain.getSize();

		final int[] limits = new int[length];
		final int[] assign = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < length; ++vindex) {
			stride[vindex] = domain.getStrides()[vindex];
		}

		for (int vindex = 0; vindex < factor.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(domain.getVariables(), factor.getDomain().getVariables()[vindex]);
			stride[offset] += ((long) factor.getDomain().getStrides()[vindex] << 32L);
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = domain.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		long idx = 0;
		double[] result = new double[domain.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			result[i] = op.f((F) this, (int) (idx & 0xFFFFFFFF), factor, (int) (idx >>> 32L));

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

		return builder.get(domain, result);
	}

	/**
	 * <p>
	 * Reduces the domain by removing a variable and selecting the specified state.
	 * </p>
	 *
	 * <p>
	 * This default implementation uses a {@link SimpleBayesianFilter} as {@link BayesianCollector} and checks if this
	 * factor {@link #isLog()}: if true builds a {@link BayesianDefaultFactor} else a {@link BayesianDefaultFactor}.
	 * </p>
	 *
	 * @param variable the variable to be filtered out
	 * @param state    the state to be selected
	 * @return if we are in log-space, a {@link BayesianLogFactor} else a {@link BayesianDefaultFactor}
	 */
	@Override
	public BayesianAbstractFactor filter(int variable, int state) {
		final int offset = domain.indexOf(variable);
		final int stride = domain.getStrideAt(offset);

		if (isLog())
			return collect(offset, BayesianLogFactor::new, new SimpleBayesianFilter(stride, state));
		else
			return collect(offset, BayesianDefaultFactor::new, new SimpleBayesianFilter(stride, state));
	}

	/**
	 * <p>
	 * Marginalize a variable out of the factor. This corresponds to sum all parameters that differ only in the state of
	 * the marginalized variable.
	 * </p>
	 *
	 * <p>
	 * If this factor represent a Conditional Probability Table you should only marginalize variables on the right side
	 * of the conditioning bar. If so, there is no need for further normalization.
	 * </p>
	 *
	 * <p>
	 * This default implementation checks if this factor {@link #isLog()} then if true, uses a {@link LogBayesianMarginal}
	 * as {@link BayesianCollector} and produces a {@link BayesianLogFactor}, otherwise it uses a {@link SimpleBayesianMarginal}
	 * and produces a {@link BayesianDefaultFactor}.
	 * </p>
	 *
	 * @param variable the variable to be summed out of the CPT
	 * @return if we are in log-space, a new {@link BayesianLogFactor}, else a new {@link BayesianDefaultFactor}, both
	 * with the variable marginalized out.
	 */
	@Override
	public BayesianAbstractFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (offset == -1) return this;

		final int size = domain.getSizeAt(offset);
		final int stride = domain.getStrideAt(offset);

		if (isLog())
			return collect(offset, BayesianLogFactor::new, new LogBayesianMarginal(size, stride));
		else
			return collect(offset, BayesianDefaultFactor::new, new SimpleBayesianMarginal(size, stride));
	}

	/**
	 * <p>
	 * This generic implementation checks if {@link #isLog()} the current factor and also the given one. If both are
	 * false, then a {@link SimpleBayesianOperation} algebra will be used and a {@link BayesianDefaultFactor} will be
	 * produced; otherwise a {@link LogBayesianOperation} algebra will be used, the second factor will be converted to
	 * a {@link BayesianLogFactor} (if needed) and a {@link BayesianLogFactor} will be returned.
	 * </p>
	 *
	 * <p>
	 * The {@link BayesianOperation#combine(BayesianFactor, int, BayesianFactor, int)} method will be used.
	 * </p>
	 *
	 * @param factor the other factor to combine with
	 * @return a {@link BayesianLogFactor} if this factor works in log-space, otherwise a {@link BayesianDefaultFactor}
	 */
	@Override
	public BayesianAbstractFactor combine(BayesianFactor factor) {
		BayesianAbstractFactor two = (BayesianAbstractFactor) factor;

		final boolean oneIsLog = this.isLog();
		final boolean twoIsLog = factor.isLog();
		final BayesianOperation<BayesianAbstractFactor> ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleBayesianOperation<>();

			return combine(two, BayesianDefaultFactor::new, ops::combine);
		} else {
			ops = new LogBayesianOperation<>();

			if (!factor.isLog())
				two = new BayesianLogFactor(factor);

			return combine(two, BayesianLogFactor::new, ops::combine);
		}
	}

	/**
	 * <p>
	 * This generic implementation checks if {@link #isLog()} the current factor and also the given one. If both are
	 * false, then a {@link SimpleBayesianOperation} algebra will be used and a {@link BayesianDefaultFactor} will be
	 * produced; otherwise a {@link LogBayesianOperation} algebra will be used, the second factor will be converted to
	 * a {@link BayesianLogFactor} (if needed) and a {@link BayesianLogFactor} will be returned.
	 * </p>
	 *
	 * <p>
	 * The {@link BayesianOperation#add(BayesianFactor, int, BayesianFactor, int)} method will be used.
	 * </p>
	 *
	 * @param factor the other factor to combine with
	 * @return a {@link BayesianLogFactor} if this factor works in log-space, otherwise a {@link BayesianDefaultFactor}
	 */
	@Override
	public BayesianAbstractFactor addition(BayesianFactor factor) {
		BayesianAbstractFactor two = (BayesianAbstractFactor) factor;

		final boolean oneIsLog = this.isLog();
		final boolean twoIsLog = factor.isLog();
		final BayesianOperation<BayesianAbstractFactor> ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleBayesianOperation<>();

			return combine(two, BayesianDefaultFactor::new, ops::add);
		} else {
			ops = new LogBayesianOperation<>();

			if (!factor.isLog())
				two = new BayesianLogFactor(factor);

			return combine(two, BayesianLogFactor::new, ops::add);
		}
	}

	/**
	 * <p>
	 * This generic implementation checks if {@link #isLog()} the current factor and also the given one. If both are
	 * false, then a {@link SimpleBayesianOperation} algebra will be used and a {@link BayesianDefaultFactor} will be
	 * produced; otherwise a {@link LogBayesianOperation} algebra will be used, the second factor will be converted to
	 * a {@link BayesianLogFactor} (if needed) and a {@link BayesianLogFactor} will be returned.
	 * </p>
	 *
	 * <p>
	 * The {@link BayesianOperation#divide(BayesianFactor, int, BayesianFactor, int)} method will be used.
	 * </p>
	 *
	 * @param factor the other factor to combine with
	 * @return a {@link BayesianLogFactor} if this factor works in log-space, otherwise a {@link BayesianDefaultFactor}
	 */
	@Override
	public BayesianAbstractFactor divide(BayesianFactor factor) {
		BayesianAbstractFactor two = (BayesianAbstractFactor) factor;

		final boolean oneIsLog = this.isLog();
		final boolean twoIsLog = factor.isLog();
		final BayesianOperation<BayesianAbstractFactor> ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleBayesianOperation<>();

			return divide(two, BayesianDefaultFactor::new, ops::divide);
		} else {
			ops = new LogBayesianOperation<>();

			if (!factor.isLog())
				two = new BayesianLogFactor(factor);

			return divide(two, BayesianLogFactor::new, ops::divide);
		}
	}

	@Override
	public String toString() {
		return "P(" + Arrays.toString(domain.getVariables()) + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BayesianAbstractFactor)) return false;

		BayesianAbstractFactor other = (BayesianAbstractFactor) obj;
		return !Arrays.equals(domain.getVariables(), other.getDomain().getVariables());
	}

	@Override
	public int hashCode() {
		return Objects.hash(domain);
	}

}
