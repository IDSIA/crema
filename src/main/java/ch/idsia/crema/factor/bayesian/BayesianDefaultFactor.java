package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.model.vertex.Collector;
import ch.idsia.crema.model.vertex.Filter;
import ch.idsia.crema.model.vertex.Marginal;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleBiFunction;
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

	protected final ToDoubleBiFunction<BayesianFactor, Integer> direct = (f, i) -> ((BayesianDefaultFactor) f).data[i];

	public BayesianDefaultFactor(Domain domain, double[] data) {
		super(domain);
		setData(data);
	}

	public BayesianDefaultFactor(Strides stride, double[] data) {
		super(stride);
		setData(data);
	}

	public BayesianDefaultFactor(int[] domain, int[] sizes, double[] data) {
		super(new Strides(domain, sizes));
		setData(data);
	}

	/**
	 * Factors are only mutable in their data. The domain should not change in
	 * time. We can therefore use the original domain.
	 */
	@Override
	public BayesianDefaultFactor copy() {
		return new BayesianDefaultFactor(domain, data);
	}

	/**
	 * Set the CPT's data specifying a custom ordering used in the data
	 */
	public void setData(final int[] domain, double[] data) {
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

		double[] target = data.clone();

		for (int index = 0; index < combinations; ++index) {
			int other_index = iterator.next();
			target[other_index] = data[index];
		}

		setData(target);
	}

	public void setData(double[] data) {
		this.data = data;
	}

	public void setValue(double value, int... states) {
		setValueAt(value, domain.getOffset(states));
	}

	public void setValueAt(double value, int index) {
		data[index] = value;
	}

	@Override
	public double getValueAt(int index) {
		return data[index];
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

		public void set(double v) {
			BayesianDefaultFactor.this.setValue(v, states);
		}

		public void add(double v) {
			double x = BayesianDefaultFactor.this.getValue(states);
			BayesianDefaultFactor.this.setValue(x + v, states);
		}
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
	 * Reduce the domain by removing a variable and selecting the specified
	 * state.
	 *
	 * @param variable the variable to be filtered out
	 * @param state    the state to be selected
	 */
	@Override
	public BayesianDefaultFactor filter(int variable, int state) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Filter(domain.getStrideAt(offset), state), BayesianDefaultFactor::new);
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
	public BayesianDefaultFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (offset == -1) return this;
		return collect(offset, new Marginal(domain.getSizeAt(offset), domain.getStrideAt(offset)), BayesianDefaultFactor::new);
	}

	protected <F extends BayesianDefaultFactor> F collect(final int offset, final Collector collector, BayesianFactorBuilder<F> builder) {
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

			new_data[target] = collector.collect(data, source);
		}

		return builder.get(target_domain, new_data);
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

	@Override
	public BayesianDefaultFactor combine(BayesianFactor factor) {
		if (factor instanceof BayesianDefaultFactor)
			return combine(factor, BayesianDefaultFactor::new, direct, direct, (a, b) -> a * b);

		return combine(factor, BayesianDefaultFactor::new, direct, BayesianFactor::getValueAt, (a, b) -> a * b);
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
	 * @param factor
	 * @return
	 */
	@Override
	public BayesianDefaultFactor addition(BayesianFactor factor) {
		if (factor instanceof BayesianDefaultFactor)
			return combine(factor, BayesianDefaultFactor::new, direct, direct, Double::sum);

		return combine(factor, BayesianDefaultFactor::new, direct, BayesianFactor::getValueAt, Double::sum);
	}


	@Override
	public BayesianDefaultFactor divide(BayesianFactor factor) {
		if (factor instanceof BayesianDefaultFactor)
			return combine(factor, BayesianDefaultFactor::new, direct, direct, (a, b) -> a / b);

		return combine(factor, BayesianDefaultFactor::new, direct, BayesianFactor::getValueAt, (a, b) -> a / b);
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


	public double KLDivergence(BayesianDefaultFactor approx) {
		IndexIterator it = approx.getDomain().getReorderedIterator(getDomain().getVariables());
		double kl = 0;
		for (int i = 0; i < data.length; i++) {
			int j = it.next();
			double p = data[i];
			double q = approx.data[j];

			kl += p * (Math.log(p) - Math.log(q));
		}
		return kl;
	}

	public BayesianDefaultFactor fixPrecision(int num_decimals, int... left_vars) {
		Strides left = getDomain().intersection(left_vars);
		Strides right = getDomain().remove(left);

		BayesianDefaultFactor newFactor = this.reorderDomain(left.concat(right));

		double[][] newData = new double[right.getCombinations()][left.getCombinations()];
		double[][] oldData = ArraysUtil.reshape2d(newFactor.data, right.getCombinations());

		for (int i = 0; i < right.getCombinations(); i++) {
			newData[i] = ArraysUtil.roundNonZerosToTarget(oldData[i], 1.0, num_decimals);
		}

		newFactor.setData(Doubles.concat(newData));
		return newFactor.reorderDomain(this.getDomain());
	}

	public boolean isMarginalNormalized() {
		if (getDomain().getVariables().length > 1)
			return false;
		return this.marginalize(this.getDomain().getVariables()[0]).getValue() == 1;
	}

	public BayesianDefaultFactor reorderDomain(Strides newStrides) {
		if (!(getDomain().isConsistentWith(newStrides) && getDomain().getSize() == newStrides.getSize())) {
			throw new IllegalArgumentException("ERROR: wrong input Strides");
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

		return this.reorderDomain(
				new Strides(
						all_vars,
						IntStream.of(all_vars).map(v -> this.getDomain().getCardinality(v)).toArray()));
	}

	public void sortDomain() {
		if (!ArrayUtils.isSorted(this.getDomain().getVariables())) {
			BayesianDefaultFactor sorted = this.reorderDomain(this.getDomain().sort());
			this.domain = sorted.domain;
			this.data = sorted.data;
		}
	}

	public BayesianDefaultFactor renameDomain(int... new_vars) {
		return new BayesianDefaultFactor(new Strides(new_vars, getDomain().getSizes()), data.clone());
	}

	@Override
	public void replaceInplace(double value, double replacement) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == value)
				setValueAt(replacement, i);
	}

	@Override
	public BayesianDefaultFactor replace(double value, double replacement) {
		BayesianDefaultFactor f = copy();
		f.replaceInplace(value, replacement);
		return f;
	}

	@Override
	public BayesianDefaultFactor replaceNaN(double replacement) {
		BayesianDefaultFactor f = copy();
		for (int i = 0; i < f.data.length; i++)
			if (Double.isNaN(f.data[i]))
				setValueAt(replacement, i);
		return f;
	}

	public boolean isDeterministic(int... given) {
		if (!DoubleStream.of(data).allMatch(x -> x == 0.0 || x == 1.0))
			return false;

		int[] left = ArraysUtil.difference(getDomain().getVariables(), given);

		BayesianDefaultFactor f = this;
		for (int v : left) {
			f = f.marginalize(v);
		}

		return DoubleStream.of(f.data).allMatch(x -> x == 1.0);
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
	public ObservationBuilder sample() {
		double[] probs = this.data;
		if (this.getDomain().getVariables().length > 1) {
			double sum = DoubleStream.of(probs).sum();
			probs = DoubleStream.of(probs).map(p -> p / sum).toArray();
		}
		return getDomain().observationOf(RandomUtil.sampleCategorical(probs));
	}

	public BayesianDefaultFactor scalarMultiply(double k) {
		BayesianDefaultFactor f = copy();
		for (int i = 0; i < f.data.length; i++) {
			f.setValueAt(f.getValueAt(i) * k, i);
		}
		return f;
	}

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param factors
	 * @return
	 */
	public static BayesianDefaultFactor combineAll(BayesianDefaultFactor... factors) {
		if (factors.length < 1)
			throw new IllegalArgumentException("wrong number of factors");
		else if (factors.length == 1)
			return factors[0].copy();

		BayesianDefaultFactor out = factors[0];
		for (int i = 1; i < factors.length; i++) {
			out = out.combine(factors[i]);
		}
		return out;
	}

	public static BayesianDefaultFactor combineAll(Collection<BayesianDefaultFactor> factors) {
		return combineAll(factors.toArray(BayesianDefaultFactor[]::new));
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
