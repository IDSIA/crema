package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import com.google.common.primitives.Ints;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:46
 */
public class VertexDeterministicFactor extends VertexDefaultFactor {

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros).
	 * Thus, children variables are determined by the values of the parents
	 *
	 * @param left        Strides - children variables.
	 * @param right       Strides - parent variables
	 * @param assignments assignments of each combination of the parent
	 * @return
	 */
	public static VertexDeterministicFactor deterministic(Strides left, Strides right, int... assignments) {

		if (assignments.length != right.getCombinations())
			throw new IllegalArgumentException("ERROR: length of assignments should be equal to the number of combinations of the parents");

		if (Ints.min(assignments) < 0 || Ints.max(assignments) >= left.getCombinations())
			throw new IllegalArgumentException("ERROR: assignments of deterministic function should be in the inteval [0," + left.getCombinations() + ")");

		VertexDeterministicFactor f = new VertexDeterministicFactor(left, right);

		for (int i = 0; i < right.getCombinations(); i++) {
			double[] values = new double[left.getCombinations()];
			values[assignments[i]] = 1.0;
			f.addVertex(values, i);
		}
		return f;
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 *
	 * @param left       Strides - children variables.
	 * @param assignment int - single value to assign
	 * @return
	 */
	public static VertexFactor deterministic(Strides left, int assignment) {
		return VertexFactor.deterministic(left, Strides.empty(), assignment);
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 *
	 * @param var        int - id for the single children variable.
	 * @param assignment int - single value to assign
	 * @return
	 */
	public VertexFactor getDeterministic(int var, int assignment) {
		return VertexFactor.deterministic(getDomain().intersection(var), assignment);
	}
}
