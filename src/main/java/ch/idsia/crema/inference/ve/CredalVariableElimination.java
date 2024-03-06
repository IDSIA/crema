package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.credal.vertex.separate.VertexAbstractFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.Observe;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.hull.ConvexHull;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.apache.commons.lang3.ArrayUtils;


public class CredalVariableElimination implements Inference<GraphicalModel<VertexFactor>, VertexFactor> {

	private ConvexHull convexHullMarg = null;

	/**
	 * @param convexHullMarg the {@link ConvexHull} method to use
	 * @return
	 */
	public CredalVariableElimination setConvexHullMarg(ConvexHull convexHullMarg) {
		this.convexHullMarg = convexHullMarg;
		return this;
	}

	protected GraphicalModel<VertexFactor> getInferenceModel(GraphicalModel<VertexFactor> model, Int2IntMap evidence, int target) {
		Observe<VertexFactor> cutObserved = new Observe<>();
		// run making a copy of the model
		GraphicalModel<VertexFactor> infModel = cutObserved.execute(model, evidence);

		RemoveBarren<VertexFactor> removeBarren = new RemoveBarren<>();
		// no more need to make a copy of the model
		removeBarren.executeInPlace(infModel, evidence, target);

		return infModel;
	}

	/**
	 * Query K(target|evidence) in the model provided to the constructor
	 *
	 * @param query    int the target variable
	 * @param evidence {@link TIntIntMap} a map of evidence in the form variable-state
	 * @return the result of the query
	 */
	@Override
	public VertexFactor query(GraphicalModel<VertexFactor> model, Int2IntMap evidence, int query) {
		GraphicalModel<VertexFactor> infModel = getInferenceModel(model, evidence, query);

		Int2IntMap filteredEvidence = new Int2IntOpenHashMap(evidence);

		// update the evidence
		for (int v : evidence.keySet()) {
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
		ConvexHull old_method = VertexAbstractFactor.getConvexHullMarg();
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
