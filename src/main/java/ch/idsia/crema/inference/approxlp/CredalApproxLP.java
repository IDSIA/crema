package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;


public class CredalApproxLP<F extends FilterableFactor<F>> implements Inference<GraphicalModel<F>, IntervalFactor> {

	private GraphicalModel<F> model;

	public CredalApproxLP() {
	}

	@Deprecated
	public CredalApproxLP(GraphicalModel<F> model) {
		setModel(model);
	}

	public void setModel(GraphicalModel<F> model) {
		this.model = model;
	}

	@Deprecated
	public GraphicalModel<F> getInferenceModel(int target, TIntIntMap evidence) {
		// preprocessing
		final CutObserved<F> cut = new CutObserved<>();
		final GraphicalModel<F> cutted = cut.execute(model, evidence);

		RemoveBarren<F> removeBarren = new RemoveBarren<>();
		return removeBarren.execute(cutted, evidence, target);
	}

	@Deprecated
	public IntervalFactor query(int target) throws InterruptedException {
		return query(target, new TIntIntHashMap());
	}

	@Deprecated
	public IntervalFactor query(int target, TIntIntMap evidence) throws InterruptedException {
		return query(model, evidence, target);
	}

	@Override
	public IntervalFactor query(GraphicalModel<F> model, TIntIntMap evidence, int query) {
		setModel(model);
		final GraphicalModel<F> infModel = getInferenceModel(query, evidence);
		final TIntIntMap filteredEvidence = new TIntIntHashMap();

		// update the evidence
		for (int v : evidence.keys()) {
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
