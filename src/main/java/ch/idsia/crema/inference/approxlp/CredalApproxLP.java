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

        CutObserved cutObserved = new CutObserved();
        // run making a copy of the model
        M infModel = cutObserved.execute(model, evidence);

        // preprocessing
        RemoveBarren removeBarren2 = new RemoveBarren();
        M infModel2 = (M) removeBarren2
                .execute(new CutObservedSepHalfspace().execute((SparseModel) infModel, evidence), target, evidence);


        RemoveBarren removeBarren = new RemoveBarren();
        // no more need to make a copy of the model
        removeBarren.executeInline(infModel, target, evidence);

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
