package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 11:11
 */
public class BayesianFactorFactory {
	private double[] data = null;
	private double[] logData = null;

	private Strides domain = Strides.empty();

	private BayesianFactorFactory() {
	}

	/**
	 * @param domain the domain where this factor is defined
	 * @return a {@link BayesianDefaultFactor} defined on the given domain with a single state equal to 1.
	 */
	public static BayesianDefaultFactor one(Domain domain) {
		return new BayesianDefaultFactor(domain, new double[]{1.});
	}

	/**
	 * * @param domain the domain where this factor is defined domain
	 *
	 * @return a {@link BayesianDefaultFactor} defined on the given domain with a single state equal to 0.
	 */
	public static BayesianDefaultFactor zero(Domain domain) {
		return new BayesianDefaultFactor(domain, new double[]{0.});
	}

	/**
	 * This is the entry point for the chained interface of this factory.
	 *
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public static BayesianFactorFactory factory() {
		return new BayesianFactorFactory();
	}

	/**
	 * @param domain set the domain of the factor
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory domain(Domain domain) {
		this.domain = Strides.fromDomain(domain);
		return data();
	}

	/**
	 * @param domain the variables that defines the domain
	 * @param sizes  the sizes of each variable
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory domain(int[] domain, int[] sizes) {
		this.domain = new Strides(domain, sizes);
		return data();
	}

	/**
	 * Set an empty data set with all the combinations of the given domain.
	 *
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory data() {
		this.data = new double[domain.getCombinations()];
		return this;
	}

	/**
	 * @param data an array of values that will be directly used
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory data(double[] data) {
		final int expectedLength = domain.getCombinations();
		if (data.length > expectedLength)
			throw new IllegalArgumentException("Invalid length of data: expected " + expectedLength + " got " + data.length);

		if (this.data == null)
			this.data = new double[expectedLength];

		// TODO: do we want to allow to assign LESS data than expected? For now yes
		System.arraycopy(data, 0, this.data, 0, data.length);
		return this;
	}

	/**
	 * @param data an array of values in log-space that will be directly used
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory logData(double[] data) {
		final int expectedLength = domain.getCombinations();
		if (data.length != expectedLength)
			throw new IllegalArgumentException("Invalid length of data: expected " + expectedLength + " got " + data.length);

		if (this.logData == null)
			this.logData = new double[expectedLength];

		// TODO: see data(double[])
		System.arraycopy(data, 0, this.logData, 0, data.length);
		return this;
	}

	/**
	 * @param domain the order of the variables that defines the values
	 * @param data   the values to use specified with the given domain order
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory data(int[] domain, double[] data) {
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

		return data(target);
	}

	/**
	 * @param value  a single value to set
	 * @param states the states that defines this value
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory value(double value, int... states) {
		data[domain.getOffset(states)] = value;
		return this;
	}

	/**
	 * @param d     a single value to set
	 * @param index the index (or offset) of the value to set
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory valueAt(double d, int index) {
		data[index] = d;
		return this;
	}

	/**
	 * @param d     a single value to set
	 * @param index the index (or offset) of the value to set
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory valuesAt(double[] d, int index) {
		System.arraycopy(d, 0, data, index, d.length);
		return this;
	}

	/**
	 * @param value  a single value to set
	 * @param states the states that defines this value
	 * @return a {@link BayesianFactorFactory} object that can be used to chain multiple commands.
	 */
	public BayesianFactorFactory set(double value, int... states) {
		return value(value, states);
	}

	/**
	 * @return a {@link BayesianLogFactor}, where the given data are converted to log-space
	 */
	public BayesianLogFactor log() {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final IndexIterator it = domain.getReorderedIterator(vars);

		final boolean isLog = logData != null;
		final double[] src = isLog ? logData : data;
		final double[] d = new double[src.length];

		for (int i = 0; i < d.length; i++) {
			d[i] = src[it.next()];
		}

		return new BayesianLogFactor(new Strides(vars, sizes), d, isLog);
	}

	/**
	 * @return a {@link BayesianDefaultFactor}, where the given data are converted to non-log-space.
	 */
	public BayesianDefaultFactor get() {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final IndexIterator it = domain.getReorderedIterator(vars);

		final double[] d = new double[data.length];

		for (int i = 0; i < d.length; i++) {
			d[i] = data[it.next()];
		}

		return new BayesianDefaultFactor(new Strides(vars, sizes), d);
	}

}
