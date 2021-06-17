package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactorUtilities;
import ch.idsia.crema.factor.convert.BayesianToVertex;
import ch.idsia.crema.utility.hull.ConvexHull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 22:18
 */
public class VertexFactorUtilities {

	public static VertexFactor mergeVertices(VertexFactor... factors) {
		if (factors.length == 0)
			throw new IllegalArgumentException("Wrong number of factors");

		else if (factors.length == 1)
			return factors[0].copy();

		return factors[0].merge(Arrays.stream(factors, 1, factors.length).toArray(VertexFactor[]::new));
	}

	/**
	 * Method for generating a random VertexFactor of a conditional credal set.
	 *
	 * @param leftDomain:   strides of the conditioned variables.
	 * @param rightDomain:  strides of the conditioning variables.
	 * @param k:            number of vertices
	 * @return
	 */
	public static VertexFactor random(Strides leftDomain, Strides rightDomain, int k) {
		// array for storing the vertices
		double[][][] data = new double[rightDomain.getCombinations()][][];

		// generate independently for each parent
		for (int i = 0; i < data.length; i++)
			data[i] = random(leftDomain, k).getVerticesAt(0);

		// build final factor
		return new VertexDefaultFactor(leftDomain, rightDomain, data);
	}

	/**
	 * Method for generating a random VertexFactor of a joint credal set.
	 *
	 * @param leftDomain:   strides of the variables.
	 * @param k:            number of vertices
	 * @return
	 */
	public static VertexFactor random(Strides leftDomain, int k) {
		if (leftDomain.getVariables().length > 1)
			throw new IllegalArgumentException("leftDomain must have only one variable.");

		int leftVar = leftDomain.getVariables()[0];

		// Binary variables can only have up to 2 points
		if (leftDomain.getCardinality(leftVar) == 2)
			k = Math.min(k, 2);

		// generate k precise factor
		List<VertexFactor> PMFs = IntStream.range(0, k - 1)
				.mapToObj(i -> BayesianFactorUtilities.random(leftDomain, Strides.empty()))
				.map(f -> new BayesianToVertex().apply(f, leftVar))
				.collect(Collectors.toList());

		// merge and convex hull
		VertexFactor out;
		do {
			if (k > 1) {
				// generate a new distribution
				VertexFactor f = new BayesianToVertex().apply(
						BayesianFactorUtilities.random(leftDomain, Strides.empty()),
						leftVar);
				PMFs.add(f);
			}

			// merge all the PMFs
			out = VertexFactorUtilities
					.mergeVertices(PMFs.toArray(VertexFactor[]::new))
					.convexHull(ConvexHull.Method.DEFAULT);

		} while (out.getVerticesAt(0).length < k);

		return out;
	}

}
