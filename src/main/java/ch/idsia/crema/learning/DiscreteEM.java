package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.JoinInference;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.learning.ExpectationMaximization;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;

public abstract  class DiscreteEM<M extends ExpectationMaximization>
            extends ExpectationMaximization<BayesianFactor, M> {


    protected JoinInference<BayesianFactor, BayesianFactor> getDefaultInference(GraphicalModel<BayesianFactor> model, int[] elimSeq) {
        return (m, query, obs) ->
        {
            CutObserved co = new CutObserved();
            GraphicalModel coModel = co.execute(m, obs);

            RemoveBarren rb = new RemoveBarren();
            GraphicalModel infModel = rb.execute(coModel, query, obs);
            rb.filter(elimSeq);

            FactorVariableElimination fve = new FactorVariableElimination(elimSeq);
            fve.setNormalize(true);

            return (BayesianFactor) fve.apply(infModel, query, obs);

        };
    }
}





