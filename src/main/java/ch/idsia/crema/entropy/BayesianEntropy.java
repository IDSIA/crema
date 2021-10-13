package ch.idsia.crema.entropy;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.IndexIterator;

import static org.apache.commons.math3.util.FastMath.log;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.12.2020 10:38
 */
public class BayesianEntropy {

	/**
	 * Compute H(A).
	 *
	 * @param A P(A) must be a marginal.
	 * @return The entropy of this CPT.
	 */
	public static double H(BayesianFactor A) {
		double H = 0;
		int x = 0;

		IndexIterator it = A.getDomain().getIterator();
		while (it.hasNext()) {
			double v = A.getValueAt(it.next());
			if (v > 0)
				H += v * log(v);
			x++;
		}

		return -H / log(x);
	}

	/**
	 * Compute H(B|A).
	 *
	 * @param fA P(A) must be a marginal.
	 * @param fB P(B|A) must be conditioned over A.
	 * @return The entropy of this relation.
	 */
	public static double H(BayesianFactor fA, BayesianFactor fB) {
		double H = 0.0;

		IndexIterator itA = fA.getDomain().getIterator();
		int x = 0;
		while (itA.hasNext()) {
			double a = fA.getValueAt(itA.next());

			IndexIterator itB = fB.getDomain().getIterator();
			x = 0;
			while (itB.hasNext()) {
				double b = fB.getValueAt(itB.next());

				// H = P(A) * P(B|A) * Log2 P(B|A)
				if (b > 0)
					H += a * b * log(b);
				x++;
			}
		}

		return -H / log(x);
	}
}
