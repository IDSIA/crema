package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import com.google.common.primitives.Ints;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:46
 */
public class VertexDeterministicFactor extends VertexDefaultFactor {

	public VertexDeterministicFactor(Strides separatedDomain, Strides vertexDomain, List<double[]> vertices, TIntList combinations) {
		super(separatedDomain, vertexDomain, vertices, combinations);
	}

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

		TIntList combinations = new TIntArrayList();
		List<double[]> vertices = new ArrayList<>();

		for (int i = 0; i < right.getCombinations(); i++) {
			double[] vertex = new double[left.getCombinations()];
			vertex[assignments[i]] = 1.0;
			vertices.add(vertex);
			combinations.add(i);
		}

		return new VertexDeterministicFactor(left, right, vertices, combinations);
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
		return deterministic(left, Strides.empty(), assignment);
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
		return deterministic(getDomain().intersection(var), assignment);
	}
}
