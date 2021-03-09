package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObservedSepHalfspace;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

// TODO: this class works only for SeparateHalfspaceFactor?
public class CredalApproxLP<F extends Factor<F>> implements Inference<GraphicalModel<F>, IntervalFactor> {

	private GraphicalModel<F> model;

	public CredalApproxLP() {
	}

	public CredalApproxLP(GraphicalModel<F> model) {
		this.model = model;
	}

	@Deprecated
	public GraphicalModel<SeparateHalfspaceFactor> getInferenceModel(int target, TIntIntMap evidence) {
		// preprocessing
		final CutObservedSepHalfspace cut = new CutObservedSepHalfspace();
		final GraphicalModel<SeparateHalfspaceFactor> cutted = cut.execute(model, evidence);

		RemoveBarren<SeparateHalfspaceFactor> removeBarren = new RemoveBarren<>();
		return removeBarren.execute(cutted, evidence, target);
	}

	@Deprecated
	public IntervalFactor query(int target) throws InterruptedException{
		return query(target, new TIntIntHashMap());
	}

	@Deprecated
	public IntervalFactor query(int target, TIntIntMap evidence) throws InterruptedException {
		GraphicalModel<GenericFactor> infModel = getInferenceModel(target, evidence);

		TIntIntMap filteredEvidence = new TIntIntHashMap();

		// update the evidence
		for (int v : evidence.keys()) {
			if (ArrayUtils.contains(infModel.getVariables(), v)) {
				filteredEvidence.put(v, evidence.get(v));
			}
		}

		IntervalFactor result;
		ApproxLP1<F> lp1 = new ApproxLP1<>();

		if (filteredEvidence.size() > 0) {
			int evbin = new BinarizeEvidence().executeInplace(infModel, filteredEvidence, filteredEvidence.size(), false);
			result = lp1.query(infModel, target, evbin);

		} else {
			result = lp1.query(infModel, target);
		}

		return result;
	}

	@Override
	public IntervalFactor query(M model, TIntIntMap evidence, int target) {
		// preprocessing
		final CutObservedSepHalfspace cut = new CutObservedSepHalfspace();
		final GraphicalModel<?> cutm = cut.execute(model, evidence);

		RemoveBarren rb = new RemoveBarren();
		M infModel = (M) rb.execute(cutm, target, evidence);

		TIntIntMap filteredEvidence = new TIntIntHashMap();

		// update the evidence
		for (int v : evidence.keys()) {
			if (ArrayUtils.contains(infModel.getVariables(), v)) {
				filteredEvidence.put(v, evidence.get(v));
			}
		}

		ApproxLP1<GenericFactor> approxLP1 = new ApproxLP1<>();
		IntervalFactor result;

		if (filteredEvidence.size() > 0) {
			final BinarizeEvidence be = new BinarizeEvidence();
			int evbin = be.executeInplace((GraphicalModel<GenericFactor>) infModel, filteredEvidence, filteredEvidence.size(), false);
			result = approxLP1.query(infModel, target, evbin);

		} else {
			result = approxLP1.query(infModel, target);
		}

		return result;
	}
}
