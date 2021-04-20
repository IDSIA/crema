package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.04.2021 14:07
 */
public interface ExtensiveVertexFactorBuilder<T extends ExtensiveVertexAbstractFactor> {

	T get(Strides strides, List<double[]> data);

}
