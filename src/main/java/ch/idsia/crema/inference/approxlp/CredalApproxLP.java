package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.*;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.Stream;

public class CredalApproxLP<M extends GraphicalModel<? super Factor<?>>> implements Inference<M, IntervalFactor> {

	private final M model;

	public CredalApproxLP(M model) {
		this.model = model;
	}

	@Override
	@SuppressWarnings("unchecked")
	public M getInferenceModel(int target, TIntIntMap evidence) {
		// preprocessing
		RemoveBarren removeBarren = new RemoveBarren();
		M infModel = (M) new CutObservedGeneric().execute(model, evidence);
		removeBarren.executeInline(infModel, target, evidence);
		return infModel;
	}

	@Override
	public IntervalFactor query(int target) throws InterruptedException{
		return query(target, new TIntIntHashMap());
	}

	@Override
	public IntervalFactor query(int target, TIntIntMap evidence) throws InterruptedException {

		M infModel = getInferenceModel(target, evidence);

		TIntIntMap filteredEvidence = new TIntIntHashMap();

		// update the evidence
		for (int v : evidence.keys()) {
			if (ArrayUtils.contains(infModel.getVariables(), v)) {
				filteredEvidence.put(v, evidence.get(v));
			}
		}

		IntervalFactor result;
		ch.idsia.crema.inference.approxlp.Inference lp1 = new ch.idsia.crema.inference.approxlp.Inference();

		if (filteredEvidence.size() > 0) {
			int evbin = new BinarizeEvidence().executeInline(infModel, filteredEvidence, filteredEvidence.size(), false);
			result = lp1.query(infModel, target, evbin);

		} else {
			result = lp1.query(infModel, target);
		}

		return result;
	}
}
