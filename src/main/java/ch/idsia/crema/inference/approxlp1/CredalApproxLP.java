package ch.idsia.crema.inference.approxlp1;

import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.Observe;
import ch.idsia.crema.preprocess.RemoveBarren;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.apache.commons.lang3.ArrayUtils;


public class CredalApproxLP<F extends FilterableFactor<F>> implements Inference<GraphicalModel<F>, IntervalFactor> {

	protected GraphicalModel<F> getInferenceModel(GraphicalModel<F> model, Int2IntMap evidence, int target) {
		// preprocessing
		final Observe<F> cut = new Observe<>();
		final GraphicalModel<F> cutted = cut.execute(model, evidence);

		RemoveBarren<F> removeBarren = new RemoveBarren<>();
		return removeBarren.execute(cutted, evidence, target);
	}

	@Override
	public IntervalFactor query(GraphicalModel<F> model, Int2IntMap evidence, int query) {
		final GraphicalModel<F> infModel = getInferenceModel(model, evidence, query);
		final Int2IntMap filteredEvidence = new Int2IntOpenHashMap();

		// update the evidence
		for (int v : evidence.keySet()) {
			if (ArrayUtils.contains(infModel.getVariables(), v)) {
				filteredEvidence.put(v, evidence.get(v));
			}
		}

		if (filteredEvidence.size() > 0) {
			final BinarizeEvidence<F> be = new BinarizeEvidence<>();
			be.setSize(filteredEvidence.size());
			MixedModel mixedModel = be.execute(model, filteredEvidence);
			final int evbin = be.getEvidenceNode();

			final ApproxLP1<GenericFactor> alp1 = new ApproxLP1<>();
			alp1.setEvidenceNode(evbin);
			return alp1.query(mixedModel, query);

		}

		final ApproxLP1<F> alp1 = new ApproxLP1<>();
		return alp1.query(infModel, query);
	}

}
