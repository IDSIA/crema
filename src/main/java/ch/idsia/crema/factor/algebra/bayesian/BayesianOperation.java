package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 11:43
 *
 * @param <F> type of both input factors
 */
public interface BayesianOperation<F extends BayesianFactor> {

	double add(F f1, int idx1, F f2, int idx2);

	double combine(F f1, int idx1, F f2, int idx2);

	double divide(F f1, int idx1, F f2, int idx2);

}
