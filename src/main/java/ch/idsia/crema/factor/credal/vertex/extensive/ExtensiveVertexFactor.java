package ch.idsia.crema.factor.credal.vertex.extensive;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 10:36
 */
public interface ExtensiveVertexFactor extends OperableFactor<ExtensiveVertexFactor> {

	@Override
	GenericFactor copy();

	@Override
	Strides getDomain();

	BayesianFactor getBayesianVertex(int vertex);

	List<double[]> getInternalVertices();

	double[] getVertex(int vertex);

	/**
	 * @return the number of vertices of this factor
	 */
	int size();

	@Override
	ExtensiveVertexFactor filter(int variable, int state);

	@Override
	ExtensiveVertexFactor combine(ExtensiveVertexFactor other);

	@Override
	ExtensiveVertexFactor marginalize(int variable);

	@Override
	ExtensiveVertexFactor divide(ExtensiveVertexFactor factor);

}
