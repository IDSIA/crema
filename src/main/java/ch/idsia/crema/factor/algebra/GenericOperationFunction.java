package ch.idsia.crema.factor.algebra;

import ch.idsia.crema.factor.GenericFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 12:34
 */
public interface GenericOperationFunction {

	<F extends GenericFactor> double f(F f1, int idx1, F f2, int idx2);

}
