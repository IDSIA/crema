package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class CredalApproxLP<M extends GraphicalModel<? super Factor<?>>> implements Inference<M, IntervalFactor> {
	
	@Override
	public IntervalFactor doQuery(M model, int target, TIntIntMap evidence) {
	
		CutObserved cutObserved = new CutObserved();
		// run making a copy of the model
		M processedModel = cutObserved.execute(model, evidence);

		RemoveBarren removeBarren = new RemoveBarren();
		// no more need to make a copy of the model
		removeBarren.executeInline(processedModel, target, evidence);
		
		TIntIntMap processedEvidence = new TIntIntHashMap(evidence);
		removeBarren.filter(processedEvidence);
		
		BinarizeEvidence binarizeEvidence = new BinarizeEvidence();
		binarizeEvidence.executeInline(processedModel, processedEvidence, 2, true);
		
		ch.idsia.crema.inference.approxlp.Inference inference = new ch.idsia.crema.inference.approxlp.Inference<>();
		try {
			IntervalFactor output = (IntervalFactor) inference.query(processedModel, target, binarizeEvidence.getLeafDummy());
			return output;
		} catch(InterruptedException e) {
			//XXX handle this 
			e.printStackTrace();
			return null;
		}
	}
}
