package ch.idsia.crema.entropy;

import java.util.Arrays;

import static org.apache.commons.math3.util.FastMath.log;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.12.2020 10:38
 */
// TODO: change this class to accept BayesianFactors
public class BayesianEntropy {

	/**
	 * Compute H(A).
	 *
	 * @param A P(A) the size of the array is the number of states.
	 * @return The entropy of this CPT.
	 */
	public static double H(double[] A) {
		return -Arrays.stream(A)
				.map(a -> a * log(2, a))
				.sum();
	}

	/**
	 * Compute H(B|A).
	 *
	 * @param A       P(A) the size of the array is the number of states.
	 * @param B       P(B|A) array mono-dimensional composed by BStates parts.
	 * @param BStates Number of states of B.
	 * @return The entropy of this relation.
	 */
	public static double H(double[] A, double[] B, int BStates) {
		double H = 0.0;
		for (int b = 0; b < BStates; b++) {
			for (int a = 0; a < A.length; a++) {
				// H = P(A) * P(B|A) * Log2 P(B|A)
				H += A[a] * B[a * BStates + b] * log(B[a * BStates + b]);
			}
		}

		return -H;
	}
}
