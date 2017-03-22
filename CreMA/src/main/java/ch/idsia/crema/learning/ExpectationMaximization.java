package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.SingleInference;
import ch.idsia.crema.model.GraphicalModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 14:24
 */
public class ExpectationMaximization {

    private final SingleInference<BayesianFactor, BayesianFactor> inferenceEngine;
    private GraphicalModel<BayesianFactor> model;

    public ExpectationMaximization(GraphicalModel<BayesianFactor> model,
                                   SingleInference<BayesianFactor, BayesianFactor> inferenceEngine) {
        this.model = model;
        this.inferenceEngine = inferenceEngine;
    }

    public void execute() {

        // expectation stage
        int[] variables = model.getVariables();

        BayesianFactor[] counts = new BayesianFactor[variables.length];

        for (int variable : variables) {
            int[] parents = model.getParents(variable);

            counts[variable].p(0, 0).and(0, 1).set(0.1);



            for (int parent : parents) {



            }


        }

        System.out.println();

        // maximization stage

    }
}
