package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.CutObservedSepHalfspace;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

public class CredalApproxLP<M extends GraphicalModel<? super Factor<?>>> implements Inference<M, IntervalFactor> {


    private M model;

    public CredalApproxLP(M model) {
        this.model = model;
    }

    @Override
    public M getInferenceModel(int target, TIntIntMap evidence) {
        // preprocessing
        RemoveBarren removeBarren = new RemoveBarren();
        M infModel = (M) removeBarren
                .execute(new CutObservedSepHalfspace().execute((SparseModel) model, evidence), target, evidence);

        return infModel;
    }

    @Override
    public IntervalFactor query(int target, TIntIntMap evidence) throws InterruptedException {

        M infModel = getInferenceModel(target, evidence);

        TIntIntMap filteredEvidence = new TIntIntHashMap(evidence);

        // update the evidence
        for (int v : evidence.keys()) {
            if (ArrayUtils.contains(infModel.getVariables(), v)) {
                filteredEvidence.put(v, evidence.get(v));
            }
        }


        IntervalFactor result = null;
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
