package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.credal.vertex.separate.VertexAbstractFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.hull.ConvexHull;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;


public class CredalVariableElimination implements Inference<GraphicalModel<VertexFactor>, VertexFactor> {

	private GraphicalModel<VertexFactor> model;

	private ConvexHull.Method convexHullMarg = null;

	/**
	 * @deprecated use {@link #query(GraphicalModel, TIntIntMap, int)}
	 */
	@Deprecated
	public void setModel(GraphicalModel<VertexFactor> model) {
		this.model = model;
	}

	public void setConvexHullMarg(ConvexHull.Method convexHullMarg) {
		this.convexHullMarg = convexHullMarg;
	}

	public GraphicalModel<VertexFactor> getInferenceModel(GraphicalModel<VertexFactor> model, TIntIntMap evidence, int target) {
		CutObserved<VertexFactor> cutObserved = new CutObserved<>();
		// run making a copy of the model
		GraphicalModel<VertexFactor> infModel = cutObserved.execute(model, evidence);

		RemoveBarren<VertexFactor> removeBarren = new RemoveBarren<>();
		// no more need to make a copy of the model
		removeBarren.executeInPlace(infModel, evidence, target);

		return infModel;
	}

	/**
	 * @deprecated use {@link #query(GraphicalModel, TIntIntMap, int)}
	 */
	@Deprecated
	public VertexFactor query(int target, TIntIntMap evidence) {
		return query(model, evidence, target);
	}

	/**
	 * Query K(target|evidence) in the model provided to the constructor
	 *
	 * @param query    int the target variable
	 * @param evidence {@link TIntIntMap} a map of evidence in the form variable-state
	 * @return the result of the query
	 */
	@Override
	public VertexFactor query(GraphicalModel<VertexFactor> model, TIntIntMap evidence, int query) {
		GraphicalModel<VertexFactor> infModel = getInferenceModel(model, evidence, query);

		TIntIntMap filteredEvidence = new TIntIntHashMap(evidence);

		// update the evidence
		for (int v : evidence.keys()) {
			if (ArrayUtils.contains(infModel.getVariables(), v)) {
				filteredEvidence.put(v, evidence.get(v));
			}
		}

		MinFillOrdering minfill = new MinFillOrdering();
		int[] order = minfill.apply(infModel);

		FactorVariableElimination<VertexFactor> ve = new FactorVariableElimination<>(order);
		ve.setEvidence(filteredEvidence);
		ve.setFactors(infModel.getFactors());
		ve.setNormalize(false);

		// Set the convex hull method
		ConvexHull.Method old_method = VertexAbstractFactor.getConvexHullMarg();
		VertexAbstractFactor.setConvexHullMarg(convexHullMarg);

		// run the query
		VertexFactor output = ve.run(query);

		for(double[][] d :output.getData())
			if(d.length==0)
				throw new IllegalStateException("Zero-vertices in result");

		// restore the previous convex hull method
		VertexAbstractFactor.setConvexHullMarg(old_method);

		return output.normalize();
	}

}
