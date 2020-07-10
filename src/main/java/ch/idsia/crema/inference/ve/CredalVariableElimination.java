package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;


public class CredalVariableElimination<M extends GraphicalModel<VertexFactor>> implements Inference<M, VertexFactor> {
	
	/** 
	 * Query K(target|evidence) in the model provided to the constructor 
	 * 
	 * @param target int the target variable 
	 * @param evidence {@link TIntIntMap} a map of evidence in the form variable-state
	 * @return
	 */
	public VertexFactor doQuery(M model, int target, TIntIntMap evidence) {
		
		CutObserved cutObserved = new CutObserved();
		// run making a copy of the model
		M processedModel = cutObserved.execute(model, evidence);

		RemoveBarren removeBarren = new RemoveBarren();
		// no more need to make a copy of the model
		removeBarren.executeInline(processedModel, target, evidence);
		
		TIntIntMap processedEvidence = new TIntIntHashMap(evidence);
		removeBarren.filter(processedEvidence);
		
		MinFillOrdering minfill = new MinFillOrdering();
		int[] order = minfill.apply(processedModel);
		
		FactorVariableElimination<VertexFactor> ve = new FactorVariableElimination<>(order);
		ve.setEvidence(processedEvidence);
		ve.setFactors(processedModel.getFactors());
		ve.setNormalize(false);
		
		VertexFactor output = ve.run(target);

		return output.normalize();
	}
}
