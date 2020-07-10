package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;


public class CredalVariableElimination<F extends GraphicalModel<VertexFactor>> {
	F model;
	
	public CredalVariableElimination(F model) {
		this.model = model;
	}
	
	/** 
	 * Query K(target|evidence) in the model provided to the constructor 
	 * 
	 * @param target int the target variable 
	 * @param evidence {@link TIntIntMap} a map of evidence in the form variable-state
	 * @return
	 */
	public VertexFactor doQuery(int target, TIntIntMap evidence) {
		
		CutObserved cutObserved = new CutObserved();
		F processedModel = cutObserved.execute(this.model, evidence);

		RemoveBarren removeBarren = new RemoveBarren();
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
