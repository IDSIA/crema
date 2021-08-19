package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.bayesian.BayesianOperation;
import ch.idsia.crema.factor.algebra.bayesian.SimpleBayesianFilter;
import ch.idsia.crema.factor.algebra.bayesian.SimpleBayesianMarginal;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * BayesianDefaultFactor
 *
 * @author david
 */
public class BayesianDefaultFactor extends BayesianAbstractFactor {

	protected double[] data;

	/**
	 * This is an optimized algebra that uses direct access to internal data storage.
	 */
	private final BayesianOperation<BayesianDefaultFactor> ops = new BayesianOperation<>() {
		@Override
		public double add(BayesianDefaultFactor f1, int idx1, BayesianDefaultFactor f2, int idx2) {
			return f1.data[idx1] + f2.data[idx2];
		}

		@Override
		public double combine(BayesianDefaultFactor f1, int idx1, BayesianDefaultFactor f2, int idx2) {
			return f1.data[idx1] * f2.data[idx2];
		}

		@Override
		public double divide(BayesianDefaultFactor f1, int idx1, BayesianDefaultFactor f2, int idx2) {
			return f1.data[idx1] / f2.data[idx2];
		}
	};

	/**
	 * This assumes that the data are ordered by the given domain.
	 *
	 * @param domain data domain
	 * @param data   ordered by the given domain
	 */
	public BayesianDefaultFactor(Domain domain, double[] data) {
		super(domain);
		this.data = data;
	}

	/**
	 * This assumes that the data are ordered by the given stride.
	 *
	 * @param stride data stride
	 * @param data   ordered by the given stride
	 */
	public BayesianDefaultFactor(Strides stride, double[] data) {
		super(stride);
		this.data = data;
	}

	/**
	 * This assumes that the data are ordered with the same order of the variables in the given domain.
	 *
	 * @param domain variables that compose the domain
	 * @param sizes  size of each variable
	 * @param data   ordered by the given domain
	 */
	public BayesianDefaultFactor(int[] domain, int[] sizes, double[] data) {
		super(new Strides(domain, sizes));
		this.data = data;
	}

	/**
	 * This assumes that the data are ordered with the same order of the variables in the given dataDomain parameter.
	 *
	 * @param stride     stride of this factor
	 * @param dataDomain order of the variables in the data
	 * @param data       ordered by the given dataDomain
	 */
	public BayesianDefaultFactor(Strides stride, int[] dataDomain, double[] data) {
		super(stride);
		setData(dataDomain, data);
	}

	/**
	 * This creates a new factor with the same domain and the data of the given factor. Data are recovered using the
	 * {@link #getValueAt(int)} method, they will not be in log-space, and they will follow the same domain of the given
	 * factor.
	 *
	 * @param factor factor to construct the new factor from
	 */
	public BayesianDefaultFactor(BayesianFactor factor) {
		super(factor.getDomain());
		data = new double[factor.getDomain().getCombinations()];

		for (int i = 0; i < data.length; i++) {
			data[i] = factor.getValueAt(i);
		}
	}

	/**
	 * @return a new {@link BayesianDefaultFactor} copy of this factor
	 */
	@Override
	public BayesianDefaultFactor copy() {
		// Factors are only mutable in their data. The domain should not change in time. We can therefore use the original domain.
		return new BayesianDefaultFactor(domain, ArrayUtils.clone(data));
	}

	/**
	 * Set the CPT's data specifying a custom ordering used in the data
	 */
	protected void setData(final int[] domain, double[] data) {
		int[] sequence = ArraysUtil.order(domain);

		// this are strides for the iterator so we do not need them one item longer
		int[] strides = new int[domain.length];
		int[] sizes = new int[domain.length];

		int[] this_variables = this.domain.getVariables();
		int[] this_sizes = this.domain.getSizes();
		int[] this_strides = this.domain.getStrides();

		// with sequence we can now set sorted_domain[1] = domain[sequence[1]];
		for (int index = 0; index < this_variables.length; ++index) {
			int newindex = sequence[index];
			strides[newindex] = this_strides[index];
			sizes[newindex] = this_sizes[index];
		}

		final int combinations = this.domain.getCombinations();
		IndexIterator iterator = new IndexIterator(strides, sizes, 0, combinations);

		this.data = new double[data.length];

		for (int index = 0; index < combinations; ++index) {
			int other_index = iterator.next();
			this.data[other_index] = data[index];
		}
	}

	@Override
	public double getValueAt(int index) {
		return data[index];
	}

	@Override
	public double getLogValueAt(int index) {
		return FastMath.log(getValue(index));
	}

	/**
	 * @return a <u>copy</u> of the internal data array
	 */
	@Override
	public double[] getData() {
		return ArrayUtils.clone(data);
	}

	public class Helper extends Number {
		private int[] states;

		public Helper(int left_var, int state) {
			and(left_var, state);
		}

		public Helper given(int var, int state) {
			// store the info
			return and(var, state);
		}

		public Helper and(int var, int state) {
			// store the info
			int index = getDomain().indexOf(var);
			states[index] = state;
			return this;
		}

		@Override
		public int intValue() {
			return (int) doubleValue();
		}

		@Override
		public long longValue() {
			return (long) doubleValue();
		}

		@Override
		public float floatValue() {
			return (float) doubleValue();
		}

		@Override
		public double doubleValue() {
			return BayesianDefaultFactor.this.getValue(states);
		}

		// TODO: factors are immutable, maybe use the ch.idsia.crema.factor.bayesian.BayesianFactorFactory?
//		public void set(double v) {
//			BayesianDefaultFactor.this.setValue(v, states);
//		}

//		public void add(double v) {
//			double x = BayesianDefaultFactor.this.getValue(states);
//			BayesianDefaultFactor.this.setValue(x + v, states);
//		}
	}

	public Helper p(int var, int state) {
		return new Helper(var, state);
	}

	/**
	 * Get the value of a single parameter in the bayesian factor identified by the instantiation
	 * provided as a string. Ex: p("0=1 | 1=3, 2=4")
	 *
	 * @param def the instantiation string
	 * @return the parameter's values
	 */
	public double p(String def) {
		def = def.replaceAll("\\s", "");
		final String[] steps = def.split("[|,]");
		if (steps.length != getDomain().getSize())
			throw new IllegalArgumentException("need spec of a single probability");

		int[] states = new int[getDomain().getSize()];

		for (String step : steps) {
			String[] parts = step.split("=");
			int var = Integer.parseInt(parts[0]);
			int state = Integer.parseInt(parts[1]);
			int index = getDomain().indexOf(var);
			states[index] = state;
		}
		return getValue(states);
	}

	/**
	 * Reduce the domain by removing a variable and selecting the specified state.
	 *
	 * @param variable the variable to be filtered out
	 * @param state    the state to be selected
	 * @return a new {@link BayesianDefaultFactor}
	 */
	@Override
	public BayesianDefaultFactor filter(int variable, int state) {
		final int offset = domain.indexOf(variable);
		final int stride = domain.getStrideAt(offset);

		return collect(offset, BayesianDefaultFactor::new, new SimpleBayesianFilter(stride, state));
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
	 * @return a new {@link BayesianDefaultFactor} with the variable marginalized out.
	 */
	@Override
	public BayesianDefaultFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (offset == -1) return this;

		final int size = domain.getSizeAt(offset);
		final int stride = domain.getStrideAt(offset);

		return collect(offset, BayesianDefaultFactor::new, new SimpleBayesianMarginal(size, stride));
	}

	/**
	 * Combine the provided factor with this one.
	 */
	public BayesianDefaultFactor combineIterator(BayesianDefaultFactor cpt) {
		Strides target = domain.union(cpt.domain);

		IndexIterator i1 = getDomain().getIterator(target);
		IndexIterator i2 = cpt.getDomain().getIterator(target);

		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			result[i] = data[i1.next()] * cpt.data[i2.next()];
		}

		return new BayesianDefaultFactor(target, result);
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
	 * <p>
	 * If the input factor is also a {@link BayesianDefaultFactor}, a fast algebra i used. If the input is a
	 * {@link BayesianLogFactor}, the factor will be first converted in the normal-space.
	 * </p>
	 *
	 * @param factor input factor
	 * @return a {@link BayesianDefaultFactor}, combination of the this with the other factor
	 */
	@Override
	public BayesianDefaultFactor combine(BayesianFactor factor) {
		if (factor.isLog())
			factor = ((BayesianLogFactor) factor).exp();

		if (factor instanceof BayesianDefaultFactor)
			return combine((BayesianDefaultFactor) factor, BayesianDefaultFactor::new, ops::combine);

		return (BayesianDefaultFactor) super.combine(factor);
	}

	/**
	 * <p>
	 * If the input factor is also a {@link BayesianDefaultFactor}, a fast algebra i used. If the input is a
	 * {@link BayesianLogFactor}, the factor will be first converted in the normal-space.
	 * </p>
	 *
	 * @param factor input factor
	 * @return a {@link BayesianDefaultFactor}, division of this factor with the other one
	 */
	@Override
	public BayesianDefaultFactor divide(BayesianFactor factor) {
		if (factor.isLog())
			factor = ((BayesianLogFactor) factor).exp();

		if (factor instanceof BayesianDefaultFactor)
			return combine((BayesianDefaultFactor) factor, BayesianDefaultFactor::new, ops::divide);

		return (BayesianDefaultFactor) super.divide(factor);
	}

	@Override
	public String toString() {
		return super.toString() + " " + Arrays.toString(this.data);
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;

		if (!(obj instanceof BayesianDefaultFactor)) return false;

		BayesianDefaultFactor other = (BayesianDefaultFactor) obj;
		return ArraysUtil.almostEquals(data, other.data, 0.00000001);
	}

	public BayesianDefaultFactor reorderDomain(Strides newStrides) {
		if (!(getDomain().isConsistentWith(newStrides) && getDomain().getSize() == newStrides.getSize())) {
			throw new IllegalArgumentException("Wrong input Strides");
		}

		// at position i, now we put the axis that were at varMap[i]
		int[] varMap = IntStream.of(newStrides.getVariables())
				.map(v -> ArraysUtil.indexOf(v, getDomain().getVariables()))
				.toArray();

		return new BayesianDefaultFactor(newStrides, ArraysUtil.swapVectorStrides(this.data, this.getDomain().getSizes(), varMap));
	}

	public BayesianDefaultFactor reorderDomain(int... vars) {
		int[] all_vars = Ints.concat(vars,
				IntStream.of(getDomain().getVariables())
						.filter(v -> !ArrayUtils.contains(vars, v)).toArray()
		);

		return reorderDomain(
				new Strides(
						all_vars,
						IntStream.of(all_vars).map(v -> this.getDomain().getCardinality(v)).toArray()));
	}

	public BayesianDefaultFactor sortDomain() {
		if (!ArrayUtils.isSorted(this.getDomain().getVariables())) {
			return this.reorderDomain(this.getDomain().sort());
		}
		return this;
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
	 * <p>
	 * If the input factor is also a {@link BayesianDefaultFactor}, a fast algebra i used. If the input is a
	 * {@link BayesianLogFactor}, the factor will be first converted in the normal-space.
	 * </p>
	 *
	 * @param factor input factor
	 * @return a {@link BayesianDefaultFactor}, sum of the probabilities of this with the input factor
	 */
	public BayesianDefaultFactor addition(BayesianDefaultFactor factor) {
		if (factor.isLog())
			factor = ((BayesianLogFactor) factor).exp();

		return combine(factor, BayesianDefaultFactor::new, ops::add);
	}

	/**
	 * @param value
	 * @param replacement
	 * @return
	 */
	public BayesianDefaultFactor replace(double value, double replacement) {
		final double[] data = ArrayUtils.clone(this.data);

		for (int i = 0; i < data.length; i++) {
			if (data[i] == value)
				data[i] = replacement;
		}

		return new BayesianDefaultFactor(domain, data);
	}

	/**
	 * Replaces all Not-a-Number values with the given value.
	 *
	 * @param replacement
	 * @return
	 */
	public BayesianDefaultFactor replaceNaN(double replacement) {
		final double[] data = ArrayUtils.clone(this.data);

		for (int i = 0; i < data.length; i++) {
			if (Double.isNaN(data[i]))
				data[i] = replacement;
		}

		return new BayesianDefaultFactor(domain, data);
	}

	public BayesianDefaultFactor scale(double k) {
		final double[] data = new double[this.data.length];

		for (int i = 0; i < data.length; i++) {
			data[i] = this.data[i] * k;
		}

		return new BayesianDefaultFactor(domain, data);
	}

	public int[] getAssignments(int... given) {
		int[] left = ArraysUtil.difference(getDomain().getVariables(), given);

		int leftSize = IntStream.of(left).map(v -> getDomain().getCardinality(v)).reduce(1, (a, b) -> a * b);

		int rightCombinations = data.length / leftSize;

		BayesianDefaultFactor f = reorderDomain(Ints.concat(left, given));

		double[][] data = ArraysUtil.reshape2d(f.data, rightCombinations, leftSize);
		return Ints.concat(Stream.of(data).map(v -> ArraysUtil.where(v, x -> x != 0.0)).toArray(int[][]::new));
	}

	/**
	 * Sample the distribution of this factor. In case of more than one variable in the the domain, the probabilities
	 * are normalized and hence the factor is considered to be a joint distribution.
	 *
	 * @return
	 */
	// TODO: consider to have a generic sample method
	@Override
	public ObservationBuilder sample() {
		double[] probs = this.data;
		if (this.getDomain().getVariables().length > 1) {
			double sum = DoubleStream.of(probs).sum();
			probs = DoubleStream.of(probs).map(p -> p / sum).toArray();
		}
		return getDomain().observationOf(RandomUtil.sampleCategorical(probs));
	}

	public BayesianDefaultFactor[] getMarginalFactors(int leftVar) {
		Strides left = Strides.as(leftVar, this.getDomain().getCardinality(leftVar));
		Strides right = this.getDomain().remove(leftVar);

		BayesianDefaultFactor cpt = this.reorderDomain(Ints.concat(left.getVariables(), right.getVariables()));
		int leftVarSize = cpt.getDomain().getCardinality(leftVar);
		List<Double> cpt_data = Doubles.asList(cpt.data);

		BayesianDefaultFactor[] factors = new BayesianDefaultFactor[right.getCombinations()];

		for (int i = 0; i < right.getCombinations(); i++) {
			double[] v = Doubles.toArray(cpt_data.subList(i * leftVarSize, (i + 1) * leftVarSize));
			factors[i] = new BayesianDefaultFactor(cpt.getDomain().intersection(leftVar), v);
		}

		return factors;
	}

	@Override
	public double logProb(TIntIntMap[] data, int leftVar) {
		double logprob = 0;

		int[] datavars = ObservationBuilder.getVariables(data);
		int[] vars = this.getDomain().getVariables();
		if (ArraysUtil.difference(vars, datavars).length > 1)
			throw new IllegalArgumentException("Wrong variables in data");

		if (!ArraysUtil.contains(leftVar, vars))
			throw new IllegalArgumentException("Wrong left variable");

		// filter data to variables in the current factor
		data = ObservationBuilder.filter(data, this.getDomain().getVariables());

		if (vars.length > 1) {

			Strides rightDomain = this.getDomain().remove(leftVar);
			BayesianDefaultFactor[] factors = this.getMarginalFactors(leftVar);

			for (int i = 0; i < rightDomain.getCombinations(); i++) {
				TIntIntMap[] data_i = ObservationBuilder.filter(data, rightDomain.getVariables(), rightDomain.statesOf(i));
				logprob += factors[i].logProb(data_i, leftVar);
			}

		} else {
			double[] observations = Doubles.concat(ObservationBuilder.toDoubles(data, leftVar));
			int numStates = this.getDomain().getSizeAt(0);
			int[] M = IntStream.range(0, numStates)
					.map(i -> (int) DoubleStream.of(observations)
							.filter(x -> x == i).count()).toArray();
			for (int i = 0; i < numStates; i++)
				logprob += Math.log(this.data[i]) * M[i];
		}

		return logprob;
	}

}
