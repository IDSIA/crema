package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.convert.BayesianToVertex;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactorUtilities;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.stream.Stream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 22:24
 */
public class VerticesModel extends DAGModel<VertexFactor> {

	/**
	 * Build a credal network from a set of precise models
	 *
	 * @param models
	 * @return
	 */
	public static GraphicalModel<VertexFactor> buildModel(boolean convexHull, BayesianNetwork... models) {
		for (int i = 1; i < models.length; i++) {
			if (!ArraysUtil.equals(models[0].getVariables(), models[i].getVariables(), true, true))
				throw new IllegalArgumentException("Inconsistent domains");
		}

		GraphicalModel<VertexFactor> vmodel = new DAGModel<>();

		for (int v : models[0].getVariables())
			vmodel.addVariable(v);
		for (int v : vmodel.getVariables()) {
			vmodel.addParents(v, models[0].getParents(v));

			VertexFactor f = VertexFactorUtilities.mergeVertices(Stream.of(models)
					.map(m -> new BayesianToVertex().apply(m.getFactor(v), v))
					.toArray(VertexFactor[]::new));
			if (convexHull)
				f = f.convexHull(true);

			vmodel.setFactor(v, f);
		}
		return vmodel;
	}
}
