package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.UniformRandomGenerator;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.DirichletSampler;
import org.apache.commons.rng.simple.JDKRandomWrapper;

import com.google.common.primitives.Doubles;

/**
 * Author: Claudio "Dna" Bonesana Project: crema Date: 16.04.2021 11:11
 */
public class BayesianFactorFactory {
	private double[] data = null;
	private double[] logData = null;

	private Strides domain = Strides.empty();

	private BayesianFactorFactory() {
		initRandom(0);
	}

	public BayesianFactorFactory initRandom(long seed) {
		unif = new JDKRandomWrapper(new Random(seed));
		return this;
	}

	/**
	 * @param var the variable associated with this factor. This variable will be
	 *            considered binary
	 * @return a {@link BayesianDefaultFactor} where state 1 has probability 1.0.
	 */
	public static BayesianDefaultFactor one(int var) {
		return new BayesianDefaultFactor(Strides.var(var, 2), new double[] { 0., 1. });
	}

	/**
	 * @param var the variable associated with this factor. This variable will be
	 *            considered binary
	 * @return a {@link BayesianDefaultFactor} where state 0 has probability 1.0.
	 */
	public static BayesianDefaultFactor zero(int var) {
		return new BayesianDefaultFactor(Strides.var(var, 2), new double[] { 1., 0. });
	}

	/**
	 * This is the entry point for the chained interface of this factory.
	 *
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public static BayesianFactorFactory factory() {
		return new BayesianFactorFactory();
	}

	/**
	 * Set the domain of the builder. Order is ignored and natural ordering will be
	 * used.
	 * 
	 * @param domain set the domain of the factor
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory domain(Domain domain) {
		this.domain = Strides.fromDomain(domain);
		return data();
	}

	/**
	 * @param domain the variables that defines the domain
	 * @param sizes  the sizes of each variable
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory domain(int[] domain, int[] sizes) {
		return domain(domain, sizes, false);
	}

	public BayesianFactorFactory domain(int[] domain, int[] sizes, boolean already_sorted) {
		if (!already_sorted) {
			int[] pos = ArraysUtil.order(domain);

			int[] sortedDomain = ArraysUtil.at(domain, pos);
			int[] sortedSizes = ArraysUtil.at(sizes, pos);

			this.domain = new Strides(sortedDomain, sortedSizes);
		} else {
			this.domain = new Strides(domain, sizes);
		}
		return this;
	}

	/**
	 * Set an empty data set with all the combinations of the given domain.
	 *
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory data() {
		this.data = new double[domain.getCombinations()];
		return this;
	}

	/**
	 * @param data an array of values that will be directly used
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory data(double[] data) {
		final int expectedLength = domain.getCombinations();
		if (data.length > expectedLength)
			throw new IllegalArgumentException(
					"Invalid length of data: expected " + expectedLength + " got " + data.length);

		if (this.data == null)
			this.data = new double[expectedLength];

		// TODO: do we want to allow to assign LESS data than expected? For now yes
		System.arraycopy(data, 0, this.data, 0, data.length);
		return this;
	}

	/**
	 * @param data an array of values in log-space that will be directly used
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory logData(double[] data) {
		final int expectedLength = domain.getCombinations();
		if (data.length != expectedLength)
			throw new IllegalArgumentException(
					"Invalid length of data: expected " + expectedLength + " got " + data.length);

		if (this.logData == null)
			this.logData = new double[expectedLength];

		// TODO: see data(double[])
		System.arraycopy(data, 0, this.logData, 0, data.length);
		return this;
	}

	/**
	 * @param domain the order of the variables that defines the values
	 * @param data   the values to use specified with the given domain order
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
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
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory value(double value, int[] variables, int[] states) {
		data[domain.getPartialOffset(variables, states)] = value;
		return this;
	}

	/**
	 * @param value  a single value to set
	 * @param states the states that defines this value
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory value(double value, int... states) {
		data[domain.getOffset(states)] = value;
		return this;
	}

	/**
	 * @param d     a single value to set
	 * @param index the index (or offset) of the value to set
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory valueAt(double d, int index) {
		data[index] = d;
		return this;
	}

	/**
	 * @param d     a single value to set
	 * @param index the index (or offset) of the value to set
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory valuesAt(double[] d, int index) {
		System.arraycopy(d, 0, data, index, d.length);
		return this;
	}

	/**
	 * @param value  a single value to set
	 * @param states the states that defines this value
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory set(double value, int... states) {
		return value(value, states);
	}

	
	/**
	 * @param value  a single value to set
	 * @param variables the order of the states attribute
	 * @param states the states that defines this value
	 * @return a {@link BayesianFactorFactory} object that can be used to chain
	 *         multiple commands.
	 */
	public BayesianFactorFactory set(double value, int[] variables, int[] states) {
		return value(value, states);
	}
	/**
	 * @return a {@link BayesianLogFactor}, where the given data are converted to
	 *         log-space
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
	 * @return a {@link BayesianDefaultFactor}, where the given data are converted
	 *         to non-log-space.
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

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param parent variable that is the parent of this factor
	 * @return a logic {@link BayesianNotFactor}
	 */
	public BayesianNotFactor not(int parent) {
		return new BayesianNotFactor(domain, parent);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param parent    variable that is the parent of this factor
	 * @param trueState index of the state to be considered as TRUE for the given
	 *                  parent
	 * @return a logic {@link BayesianAndFactor}
	 */
	public BayesianAndFactor not(int parent, int trueState) {
		return new BayesianAndFactor(domain, parent, trueState);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param parents variables that are parents of this factor
	 * @return a logic {@link BayesianAndFactor}
	 */
	public BayesianAndFactor and(int... parents) {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final int[] order = ArraysUtil.order(parents);

		// sort parents
		final int[] pars = new int[parents.length];

		for (int i = 0; i < order.length; i++) {
			int o = order[i];
			pars[i] = parents[o];
		}
		return new BayesianAndFactor(new Strides(vars, sizes), pars);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param trueStates index of the state to be considered as TRUE for each given
	 *                   parent
	 * @param parents    variables that are parents of this factor
	 * @return a logic {@link BayesianAndFactor}
	 */
	public BayesianAndFactor and(int[] parents, int[] trueStates) {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final int[] order = ArraysUtil.order(parents);

		// sort parents
		final int[] pars = new int[parents.length];
		final int[] trus = new int[trueStates.length];

		for (int i = 0; i < order.length; i++) {
			int o = order[i];
			pars[i] = parents[o];
			trus[i] = trueStates[o];
		}

		return new BayesianAndFactor(new Strides(vars, sizes), pars, trus);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param parents variables that are parents of this factor
	 * @return a logic {@link BayesianOrFactor}
	 */
	public BayesianOrFactor or(int... parents) {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final int[] order = ArraysUtil.order(parents);

		// sort parents
		final int[] pars = new int[parents.length];

		for (int i = 0; i < order.length; i++) {
			int o = order[i];
			pars[i] = parents[o];
		}
		return new BayesianOrFactor(new Strides(vars, sizes), pars);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param trueStates index of the state to be considered as TRUE for each given
	 *                   parent
	 * @param parents    variables that are parents of this factor
	 * @return a logic {@link BayesianOrFactor}
	 */
	public BayesianOrFactor or(int[] parents, int[] trueStates) {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final int[] order = ArraysUtil.order(parents);

		// sort parents
		final int[] pars = new int[parents.length];
		final int[] trus = new int[trueStates.length];

		for (int i = 0; i < order.length; i++) {
			int o = order[i];
			pars[i] = parents[o];
			trus[i] = trueStates[o];
		}

		return new BayesianOrFactor(new Strides(vars, sizes), pars, trus);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param parents   variables that are parents of this factor
	 * @param strengths values for the inhibition strength for each given parent
	 * @return a logic {@link BayesianNoisyOrFactor}
	 */
	public BayesianNoisyOrFactor noisyOr(int[] parents, double[] strengths) {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final int[] order = ArraysUtil.order(parents);

		// sort parents
		final int[] pars = new int[parents.length];
		final double[] inbs = new double[strengths.length];

		for (int i = 0; i < order.length; i++) {
			int o = order[i];
			pars[i] = parents[o];
			inbs[i] = strengths[o];
		}

		return new BayesianNoisyOrFactor(new Strides(vars, sizes), pars, inbs);
	}

	/**
	 * Requires a pre-defined Domain.
	 *
	 * @param parents    variables that are parents of this factor
	 * @param trueStates index of the state to be considered as TRUE for each given
	 *                   parent
	 * @param strengths  values for the inhibition strength for each given parent
	 * @return a logic {@link BayesianNoisyOrFactor}
	 */
	public BayesianNoisyOrFactor noisyOr(int[] parents, int[] trueStates, double[] strengths) {
		// sort variables
		final int[] vars = ArraysUtil.sort(domain.getVariables());
		final int[] sizes = IntStream.of(vars).map(domain::indexOf).map(domain::getSizeAt).toArray();
		final int[] order = ArraysUtil.order(parents);

		// sort parents
		final int[] pars = new int[parents.length];
		final int[] trus = new int[trueStates.length];
		final double[] inbs = new double[strengths.length];

		for (int i = 0; i < order.length; i++) {
			int o = order[i];
			pars[i] = parents[o];
			trus[i] = trueStates[o];
			inbs[i] = strengths[o];
		}

		return new BayesianNoisyOrFactor(new Strides(vars, sizes), pars, trus, inbs);
	}

	UniformRandomProvider unif;

	public BayesianFactorFactory random() {
		return random(null);
	}

	public BayesianFactorFactory random(Domain conditioning) {

		int combinations;
		Strides dom;
		int[] vars;

		if (conditioning == null) {
			combinations = 1;
			dom = this.domain;
			vars = dom.getVariables();
		} else {
			combinations = Arrays.stream(conditioning.getSizes()).reduce(1, (a, b) -> a * b);
			dom = this.domain.remove(conditioning.getVariables());
			vars = ArraysUtil.append(dom.getVariables(), conditioning.getVariables());
		}

		int size = dom.getCombinations();

		double[] alpha = new double[size];
		Arrays.fill(alpha, 1.0);

		DirichletSampler ds = DirichletSampler.of(unif, alpha);
		double[][] arr = ds.samples(combinations).toArray(double[][]::new);
		if (combinations == 1) {
			return data(vars, arr[0]);
		} else {
			return data(vars, Doubles.concat(arr));
		}
	}
}
