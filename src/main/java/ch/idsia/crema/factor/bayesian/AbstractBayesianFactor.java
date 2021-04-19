package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 18:43
 */
public abstract class AbstractBayesianFactor implements BayesianFactor {

	protected Strides domain;

	public AbstractBayesianFactor(Domain domain) {
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
	 * @param factor   other factor to combine with
	 * @param builder  Supplier that can build a new BayesianFactor from domain and data
	 * @param getThis  accessor function to this factor (left part)
	 * @param getOther accessor function to other factor (right part)
	 * @param f        product of probabilities, should be sum if log, otherwise a product
	 * @param <F>      returned type
	 * @return a factor, combination of this factor with the given other factor
	 */
	protected <F extends BayesianDefaultFactor> F combine(
			BayesianFactor factor,
			BayesianFactorBuilder<F> builder,
			ToDoubleBiFunction<BayesianFactor, Integer> getThis,
			ToDoubleBiFunction<BayesianFactor, Integer> getOther,
			BiFunction<Double, Double, Double> f
	) {
		// domains should be sorted
		this.sortDomain();
		factor = factor.copy();
		factor.sortDomain();

		final Strides target = domain.union(factor.getDomain());
		final int length = target.getSize();

		final int[] limits = new int[length];
		final int[] assign = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < domain.getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), domain.getVariables()[vindex]);
			stride[offset] = domain.getStrides()[vindex];
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
			result[i] = f.apply(getThis.applyAsDouble(this, (int) (idx & 0xFFFFFFFF)), getOther.applyAsDouble(factor, (int) (idx >>> 32L)));

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
	 * @param factor   other factor to combine with
	 * @param builder  Supplier that can build a new BayesianFactor from domain and data
	 * @param getThis  accessor function to this factor (left part)
	 * @param getOther accessor function to other factor (right part)
	 * @param f        product of probabilities, should be subtraction if log, otherwise a division
	 * @param <F>      returned type
	 * @return a factor, combination of this factor with the given other factor
	 */
	protected <F extends BayesianDefaultFactor> F divide(
			BayesianFactor factor,
			BayesianFactorBuilder<F> builder,
			ToDoubleBiFunction<BayesianFactor, Integer> getThis,
			ToDoubleBiFunction<BayesianFactor, Integer> getOther,
			BiFunction<Double, Double, Double> f
	) {
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
			result[i] = f.apply(getThis.applyAsDouble(this, (int) (idx & 0xFFFFFFFF)), getOther.applyAsDouble(factor, (int) (idx >>> 32L)));

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

	@Override
	public String toString() {
		return "P(" + Arrays.toString(domain.getVariables()) + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractBayesianFactor)) return false;

		AbstractBayesianFactor other = (AbstractBayesianFactor) obj;
		return !Arrays.equals(domain.getVariables(), other.getDomain().getVariables());
	}

	@Override
	public int hashCode() {
		return Objects.hash(domain);
	}

}
