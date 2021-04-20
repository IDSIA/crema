package ch.idsia.crema.factor.credal.vertex;

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
public interface ExtensiveVertexFactor extends OperableFactor<ExtensiveVertexAbstractFactor> {

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
	ExtensiveVertexAbstractFactor filter(int variable, int state);

	@Override
	ExtensiveVertexAbstractFactor combine(ExtensiveVertexAbstractFactor other);

	@Override
	ExtensiveVertexAbstractFactor marginalize(int variable);

	@Override
	ExtensiveVertexAbstractFactor divide(ExtensiveVertexAbstractFactor factor);

}
