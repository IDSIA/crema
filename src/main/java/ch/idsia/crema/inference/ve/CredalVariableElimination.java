package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;


public class CredalVariableElimination<M extends GraphicalModel<VertexFactor>> implements Inference<M, VertexFactor> {

	private M model;

	public CredalVariableElimination(M model){
		this.model = model;
	}


	@Override
	public M getInferenceModel(int target, TIntIntMap evidence) {
		CutObserved cutObserved = new CutObserved();
		// run making a copy of the model
		M infModel = cutObserved.execute(model, evidence);

		RemoveBarren removeBarren = new RemoveBarren();
		// no more need to make a copy of the model
		removeBarren.executeInline(infModel, target, evidence);


		return infModel;

	}

	/**
	 * Query K(target|evidence) in the model provided to the constructor 
	 * 
	 * @param target int the target variable 
	 * @param evidence {@link TIntIntMap} a map of evidence in the form variable-state
	 * @return
	 */

	 public VertexFactor query(int target, TIntIntMap evidence) {

		 M infModel =  getInferenceModel(target, evidence);

		 TIntIntMap filteredEvidence = new TIntIntHashMap(evidence);

		 // update the evidence
		 for(int v: evidence.keys()){
			 if(ArrayUtils.contains(infModel.getVariables(), v)){
				 filteredEvidence.put(v, evidence.get(v));
			 }
		 }

		MinFillOrdering minfill = new MinFillOrdering();
		int[] order = minfill.apply(infModel);
		
		FactorVariableElimination<VertexFactor> ve = new FactorVariableElimination<>(order);
		ve.setEvidence(filteredEvidence);
		ve.setFactors(infModel.getFactors());
		ve.setNormalize(false);
		
		VertexFactor output = ve.run(target);

		return output.normalize();
	}
}
