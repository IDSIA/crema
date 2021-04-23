package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.LogSpace;
import ch.idsia.crema.factor.algebra.OperationUtils;
import ch.idsia.crema.factor.algebra.bayesian.BayesianOperation;
import ch.idsia.crema.factor.algebra.bayesian.LogBayesianMarginal;
import ch.idsia.crema.factor.algebra.bayesian.SimpleBayesianFilter;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    15.04.2021 18:17
 */
@LogSpace
public class BayesianLogFactor extends BayesianDefaultFactor {

	/**
	 * This is an optimized algebra that uses direct access to internal data storage.
	 */
	private final BayesianOperation<BayesianLogFactor> ops = new BayesianOperation<>() {
		@Override
		public double add(BayesianLogFactor f1, int idx1, BayesianLogFactor f2, int idx2) {
			return OperationUtils.logSum(f1.data[idx1], f2.data[idx2]);
		}

		@Override
		public double combine(BayesianLogFactor f1, int idx1, BayesianLogFactor f2, int idx2) {
			return f1.data[idx1] + f2.data[idx2];
		}

		@Override
		public double divide(BayesianLogFactor f1, int idx1, BayesianLogFactor f2, int idx2) {
			return f1.data[idx1] - f2.data[idx2];
		}
	};

	/**
	 * This assumes that the data are ordered by the given domain and <u>not</u> in log-space.
	 *
	 * @param domain data domain
	 * @param data   ordered by the given domain
	 */
	public BayesianLogFactor(Domain domain, double[] data) {
		super(domain, ArraysUtil.log(data));
	}

	/**
	 * This assumes that the data are ordered by the given stride and <u>not</u> in log-space.
	 *
	 * @param stride data domain
	 * @param data   ordered by the given stride
	 */
	public BayesianLogFactor(Strides stride, double[] data) {
		super(stride, ArraysUtil.log(data));
	}

	/**
	 * This assumes that the data are ordered with the same order of the variables in the given domain and <u>not</u> in log-space.
	 *
	 * @param domain variables that compose the domain
	 * @param sizes  size of each variable
	 * @param data   ordered by the given domain
	 */
	public BayesianLogFactor(int[] domain, int[] sizes, double[] data) {
		super(domain, sizes, ArraysUtil.log(data));
	}

	/**
	 * This assumes that the data are ordered with the same order of the variables in the given dataDomain and <u>not</u> in log-space.
	 *
	 * @param stride     stride of this factor
	 * @param dataDomain order of the variables in the data
	 * @param data       ordered by the given dataDomain
	 */
	public BayesianLogFactor(Strides stride, int[] dataDomain, double[] data) {
		super(stride, dataDomain, ArraysUtil.log(data));
	}

	/**
	 * This assumes that the data are ordered by the given domain and <u>are already</u> in log-space.
	 *
	 * @param stride data domain
	 * @param data   ordered and already in log-space
	 * @param isLog  not used
	 */
	BayesianLogFactor(Strides stride, double[] data, boolean isLog) {
		super(stride, data);
	}

	/**
	 * This creates a new factor with the same domain and the data of the given factor. Data are copied using the
	 * {@link ArraysUtil#log(double[])} method, they will be in log-space, and they will follow the same domain of the
	 * given factor.
	 *
	 * @param factor factor to construct this new factor from
	 */
	public BayesianLogFactor(BayesianDefaultFactor factor) {
		super(factor.domain, ArraysUtil.log(factor.data));
	}

	/**
	 * This creates a new factor with the same domain and the data of the given factor. Data are recovered using the
	 * {@link #getLogValueAt(int)} method, they will not be in log-space, and they will follow the same domain of the
	 * given factor.
	 *
	 * @param factor factor to construct this new factor from
	 */
	public BayesianLogFactor(BayesianFactor factor) {
		super(factor.getDomain(), new double[factor.getDomain().getCombinations()]);

		for (int i = 0; i < data.length; i++) {
			data[i] = factor.getLogValueAt(i);
		}
	}

	@Override
	public BayesianLogFactor copy() {
		return new BayesianLogFactor(domain, ArrayUtils.clone(data), true);
	}

	@Override
	public double getValueAt(int index) {
		return FastMath.exp(data[index]);
	}

	@Override
	public double getLogValueAt(int index) {
		return data[index];
	}

	/**
	 * @return an array in normal space, log is removed
	 */
	@Override
	public double[] getData() {
		return ArraysUtil.exp(ArrayUtils.clone(this.data));
	}

	public double[] getLogData() {
		return ArrayUtils.clone(this.data);
	}

	/**
	 * Converts this factor in a {@link BayesianDefaultFactor}, same data but not in log-space.
	 *
	 * @return this same factor but not in log-space
	 */
	public BayesianDefaultFactor exp() {
		return new BayesianDefaultFactor(domain, ArraysUtil.exp(data));
	}

	/**
	 * Reduce the domain by removing a variable and selecting the specified state.
	 *
	 * @param variable the variable to be filtered out
	 * @param state    the state to be selected
	 * @return a new {@link BayesianLogFactor}
	 */
	@Override
	public BayesianLogFactor filter(int variable, int state) {
		final int offset = domain.indexOf(variable);
		final int stride = domain.getStrideAt(offset);

		return collect(offset, (dom, data) -> new BayesianLogFactor(dom, data, true), new SimpleBayesianFilter(stride, state));
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
	 * @return a new {@link BayesianLogFactor} with the variable marginalized out.
	 */
	@Override
	public BayesianLogFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (offset == -1) return this;

		final int size = domain.getSizeAt(offset);
		final int stride = domain.getStrideAt(offset);

		return collect(offset, (dom, data) -> new BayesianLogFactor(dom, data, true), new LogBayesianMarginal(size, stride));
	}

	@Override
	public BayesianLogFactor combineIterator(BayesianDefaultFactor cpt) {
		Strides target = domain.union(cpt.domain);

		IndexIterator i1 = getDomain().getIterator(target);
		IndexIterator i2 = cpt.getDomain().getIterator(target);

		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			result[i] = data[i1.next()] + cpt.data[i2.next()];
		}

		return new BayesianLogFactor(target, result);
	}

	/**
	 * <p>
	 * If the input factor is also a {@link BayesianLogFactor}, a fast algebra i used. If the input is a
	 * {@link BayesianDefaultFactor}, the factor will be first converted in the log-space.
	 * </p>
	 *
	 * @param factor input factor
	 * @return a {@link BayesianLogFactor}, combination of the this with the other factor
	 */
	@Override
	public BayesianLogFactor combine(BayesianFactor factor) {
		if (!factor.isLog() && factor instanceof BayesianDefaultFactor)
			factor = new BayesianLogFactor((BayesianDefaultFactor) factor);

		if (factor instanceof BayesianLogFactor)
			return combine((BayesianLogFactor) factor, (dom, data) -> new BayesianLogFactor(dom, data, true), ops::combine);

		return (BayesianLogFactor) super.combine(factor);
	}

	/**
	 * <p>
	 * If the input factor is also a {@link BayesianLogFactor}, a fast algebra i used. If the input is a
	 * {@link BayesianDefaultFactor}, the factor will be first converted in the log-space.
	 * </p>
	 *
	 * @param factor input factor
	 * @return a {@link BayesianLogFactor}, sum of the probabilities of this with the other factor
	 */
	@Override
	public BayesianLogFactor addition(BayesianFactor factor) {
		if (!factor.isLog() && factor instanceof BayesianDefaultFactor)
			factor = new BayesianLogFactor((BayesianDefaultFactor) factor);

		if (factor instanceof BayesianLogFactor)
			return combine((BayesianLogFactor) factor, (dom, data) -> new BayesianLogFactor(dom, data, true), ops::add);

		return (BayesianLogFactor) super.addition(factor);
	}

	/**
	 * <p>
	 * If the input factor is also a {@link BayesianLogFactor}, a fast algebra i used. If the input is a
	 * {@link BayesianDefaultFactor}, the factor will be first converted in the log-space.
	 * </p>
	 *
	 * @param factor input factor
	 * @return a {@link BayesianLogFactor}, division of the this with the other factor
	 */
	@Override
	public BayesianLogFactor divide(BayesianFactor factor) {
		if (!factor.isLog() && factor instanceof BayesianDefaultFactor)
			factor = new BayesianLogFactor((BayesianDefaultFactor) factor);

		if (factor instanceof BayesianLogFactor)
			return combine((BayesianLogFactor) factor, (dom, data) -> new BayesianLogFactor(dom, data, true), ops::divide);

		return (BayesianLogFactor) super.divide(factor);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BayesianLogFactor)) return false;

		BayesianLogFactor other = (BayesianLogFactor) obj;
		if (!Arrays.equals(domain.getVariables(), other.getDomain().getVariables())) return false;

		return ArraysUtil.almostEquals(data, other.data, 1.0E-08);
	}

	@Override
	public BayesianLogFactor replace(double value, double replacement) {
		value = FastMath.log(value);
		replacement = FastMath.log(replacement);

		final double[] data = ArrayUtils.clone(this.data);

		for (int i = 0; i < data.length; i++) {
			if (data[i] == value)
				data[i] = replacement;
		}

		return new BayesianLogFactor(domain, data);
	}

	/**
	 * Replace all {@link Double#NaN} values with the given replacement value.
	 *
	 * @param replacement replacement value in non-log space (log is applied internally)
	 * @return a new {@link BayesianLogFactor} without NaN.
	 */
	@Override
	public BayesianLogFactor replaceNaN(double replacement) {
		replacement = FastMath.log(replacement);

		final double[] data = ArrayUtils.clone(this.data);

		for (int i = 0; i < data.length; i++) {
			if (Double.isNaN(data[i]))
				data[i] = replacement;
		}

		return new BayesianLogFactor(domain, data);
	}

	/**
	 * Multiplies all value by a given constant. The data are first converted to normal-space, scaled, then converted
	 * again in log-space.
	 *
	 * @param k constant multiplier
	 * @return a new {@link BayesianLogFactor} scaled by the given constant
	 */
	@Override
	public BayesianLogFactor scale(double k) {
		final double[] data = new double[this.data.length];

		for (int i = 0; i < data.length; i++) {
			data[i] = FastMath.log(FastMath.exp(this.data[i] * k));
		}

		return new BayesianLogFactor(domain, data);
	}

	@Override
	public ObservationBuilder sample() {
		double[] probs = ArraysUtil.exp(this.data);
		if (this.getDomain().getVariables().length > 1) {
			double sum = DoubleStream.of(probs).sum();
			probs = DoubleStream.of(probs).map(p -> p / sum).toArray();
		}
		return getDomain().observationOf(RandomUtil.sampleCategorical(probs));
	}
}
