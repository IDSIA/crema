package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 15:41
 *
 *
 */
interface BayesianFactorBuilder<T extends BayesianFactor> {

	T get(Strides domain, double[] data);

}