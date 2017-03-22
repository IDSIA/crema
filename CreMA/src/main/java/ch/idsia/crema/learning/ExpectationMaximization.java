package ch.idsia.crema.learning;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;

import java.util.function.Function;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 14:24
 */
public class ExpectationMaximization<F extends Factor> {

    private SparseModel<F> model;

    public ExpectationMaximization(SparseModel<F> model) {
        this.model = model;
    }

    public void execute() {

        Function<Integer, Integer> f;

        // expectation stage
        int[] variables = model.getVariables();
        for (int variable : variables) {
            int[] parents = model.getParents(variable);

            Strides domain = model.getDomain(parents);


            System.out.print(variable + ": " + domain);

            for (int parent : parents) {
                System.out.print(parent + " ");
            }
            System.out.println();
        }



        // maximization stage

    }

    private double[] inference(int[] query, TIntIntMap evidence){
        RemoveBarren rb = new RemoveBarren();
        // P(query|evidences)
        SparseModel<F> infModel = rb.execute(model, query, evidence);

        // elimination sequence
        MinFillOrdering mfo = new MinFillOrdering();
        int[] elimSeq = mfo.apply(infModel);

        FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
//        fve.setFactors(infModel.getFactors());

        // normalize
        fve.setNormalize(true);
        fve.setEvidence(evidence);

        BayesianFactor posterior = fve.run(query);
        return posterior.getData();
    }
}
