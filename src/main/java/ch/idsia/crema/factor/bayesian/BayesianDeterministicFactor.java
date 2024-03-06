package ch.idsia.crema.factor.bayesian;


import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import com.google.common.primitives.Ints;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    15.04.2021 18:51
 */
public class BayesianDeterministicFactor extends BayesianDefaultFactor {

	/**
	 * This is used internally by the {@link #copy()} method.
	 *
	 * @param domain this object domain
	 * @param data   internal data representation
	 */
	private BayesianDeterministicFactor(Domain domain, double[] data) {
		super(domain, data);
	}

	/**
	 * Constructs a deterministic factor (values can only be ones or zeros).
	 * Thus, children variables are determined by the values of the parents
	 *
	 * @param left        Strides - children variables.
	 * @param right       Strides - parent variables
	 * @param assignments assignments of each combination of the parent
	 */
	public BayesianDeterministicFactor(Strides left, Strides right, int... assignments) {
		super(left.concat(right), null);
		final int combinations = right.getCombinations();

		if (assignments.length != combinations)
			throw new IllegalArgumentException("Length of assignments should be equal to the number of combinations of the parents");

		if (Ints.min(assignments) < 0 || Ints.max(assignments) >= left.getCombinations())
			throw new IllegalArgumentException("Assignments of deterministic function should be in the interval [0," + left.getCombinations() + ")");

		data = new double[combinations];
		for (int i = 0; i < combinations; i++) {
			data[i * left.getCombinations() + assignments[i]] = 1.0;
		}
	}

	public static BayesianDefaultFactor getJoinDeterministic(Strides left_vars, Int2IntMap obs) {
		final double[] data = new double[left_vars.getCombinations()];

		for (int index : left_vars.getCompatibleIndexes(obs))
			data[index] = 1;

		return new BayesianDeterministicFactor(left_vars, data);
	}

	/**
	 * Constructs a deterministic factor (values can only be ones or zeros) without parent variables.
	 *
	 * @param left       Strides - children variables.
	 * @param assignment int - single value to assign
	 */
	public BayesianDeterministicFactor(Strides left, int assignment) {
		this(left, Strides.empty(), assignment);
	}

	@Override
	public BayesianDeterministicFactor copy() {
		return new BayesianDeterministicFactor(domain, ArrayUtils.clone(data));
	}

	// TODO: consider to override combine-based method to keep the BayesianDeterministicFactor object (currently, this factor has nothing special)

}
