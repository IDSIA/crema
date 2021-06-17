package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;
import ch.idsia.crema.utility.hull.ConvexHull;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:19
 */
public interface VertexFactor extends OperableFactor<VertexFactor>, SeparatelySpecified<VertexFactor> {

	@Override
	VertexFactor copy();

	@Override
	VertexFactor filter(int variable, int state);

	@Override
	VertexFactor combine(VertexFactor other);

	@Override
	VertexFactor marginalize(int variable);

	@Override
	VertexFactor divide(VertexFactor factor);

	@Override
	VertexFactor normalize(int... given);

	VertexFactor getSingleVertexFactor(int... idx);

	@Override
	Strides getDomain();

	@Override
	Strides getSeparatingDomain();

	@Override
	Strides getDataDomain();

	/**
	 * @return the number of vertices in the factor
	 */
	int size();

	VertexFactor reseparate(Strides target);

	double[][] getVertices(int ...states);

	double[][] getVerticesAt(int i);

	double[][][] getData();

	VertexFactor convexHull(ConvexHull.Method m);

	VertexFactor merge(VertexFactor factor);

	default VertexFactor merge(VertexFactor... factors) {
		if (factors.length == 1)
			return merge(factors[0]);

		VertexFactor out = this;
		for (VertexFactor f : factors)
			out = out.merge(f);

		return out;
	}

	BayesianFactor sample();
}
