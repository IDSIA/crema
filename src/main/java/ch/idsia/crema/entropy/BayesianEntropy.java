package ch.idsia.crema.entropy;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.log;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.12.2020 10:38
 */
// TODO: add tests
public class BayesianEntropy {

	/**
	 * Compute H(A).
	 *
	 * @param A P(A) must be a marginal.
	 * @return The entropy of this CPT.
	 */
	public static double H(BayesianFactor A) {
		int states = A.getDomain().getCardinality(1);
		return -Arrays.stream(A.getData())
				.map(a -> a * log(states, a))
				.sum();
	}

	/**
	 * Compute H(B|A).
	 *
	 * @param fA P(A) must be a marginal.
	 * @param fB P(B|A) must be conditioned over A.
	 * @return The entropy of this relation.
	 */
	public static double H(BayesianFactor fA, BayesianFactor fB) {
		double[] A = fA.getData();
		double[] B = fB.getData();

		int AStates = fA.getDomain().getCardinality(1);
		int BStates = fB.getDomain().getCardinality(2);

		double H = 0.0;
		for (int b = 0; b < BStates; b++) {
			for (int a = 0; a < AStates; a++) {
				// H = P(A) * P(B|A) * Log2 P(B|A)
				H += A[a] * B[a * BStates + b] * log(B[a * BStates + b]);
			}
		}

		return -H;
	}
}
