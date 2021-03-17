package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:19
 */
public interface IVertexFactor extends SeparatelySpecified<IVertexFactor>, OperableFactor<IVertexFactor> {

	@Override
	IVertexFactor copy();

	IVertexFactor reseparate(Strides target);

	double[][][] getData();

}
