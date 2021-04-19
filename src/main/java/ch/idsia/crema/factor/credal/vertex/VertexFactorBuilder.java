package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 15:41
 *
 *
 */
interface VertexFactorBuilder<T extends VertexFactor> {

	T get(Strides vertexDomain, Strides separatedDomain, double[][][] data);

}