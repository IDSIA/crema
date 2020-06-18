package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.JoinInference;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 14:24
 */
public class ExpectationMaximization {

    private final JoinInference<BayesianFactor, BayesianFactor> inferenceEngine;
    private GraphicalModel<BayesianFactor> model;

    private TIntIntMap[] observations;

    public ExpectationMaximization(GraphicalModel<BayesianFactor> model,
                                   JoinInference<BayesianFactor, BayesianFactor> inferenceEngine,
                                   TIntIntMap[] observations) {
        this.model = model;
        this.inferenceEngine = inferenceEngine;
        this.observations = observations;
    }


    public ExpectationMaximization(GraphicalModel<BayesianFactor> model, int[] elimSeq, TIntIntMap[] observations){
        this(model,(m, query, obs) -> {
            CutObserved co = new CutObserved();
            GraphicalModel<BayesianFactor> coModel = co.execute(m, obs);

            RemoveBarren rb = new RemoveBarren();
            GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, query, obs);
            rb.filter(elimSeq);

            FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
            fve.setNormalize(true);

            return fve.apply(infModel, query, obs);
        }, observations);

    }

    public ExpectationMaximization(GraphicalModel<BayesianFactor> model, TIntIntMap[] observations) {
        this(model, (new MinFillOrdering()).apply(model), observations);
    }



    public void execute() throws InterruptedException {

        // expectation stage
        int[] variables = model.getVariables();
        TIntObjectMap<BayesianFactor> counts = new TIntObjectHashMap<>();
        for (int variable : variables) {
            counts.put(variable, new BayesianFactor(model.getFactor(variable).getDomain(), false));
        }

        for (TIntIntMap observation : observations) {

            for (int variable : variables) {
                int[] parents = model.getParents(variable);
                int[] unobservedParents = ArraysUtil.removeAllFromSortedArray(parents, observation.keys());
                int[] query = ArraysUtil.addToSortedArray(unobservedParents, variable);

                BayesianFactor bf1 = inferenceEngine.apply(model, query, observation);
                BayesianFactor bf2 = bf1.divide(bf1.marginalize(variable));

                IndexIterator it = counts.get(variable).getDomain().getIterator(bf2.getDomain(), observation);

                for (double d : bf2.getData()) {
                    int index = it.next();
                    double x = counts.get(variable).getValueAt(index) + d;
                    counts.get(variable).setValueAt(x, index);
                }
            }
        }

        System.out.println();

        // maximization stage

    }
}
