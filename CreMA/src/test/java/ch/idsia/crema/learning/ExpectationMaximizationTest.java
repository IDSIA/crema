package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 14:31
 */
public class ExpectationMaximizationTest {

    GraphicalModel<BayesianFactor> model;

    @Test
    public void testModelLoading() {

        model = new SparseModel<>();

        int hA = model.addVariable(2);
        int hB = model.addVariable(2);
        int hC = model.addVariable(2);

        BayesianFactor bfA = new BayesianFactor(model.getDomain(hA), false);
        BayesianFactor bfB = new BayesianFactor(model.getDomain(hA, hB), false);
        BayesianFactor bfC = new BayesianFactor(model.getDomain(hA, hB, hC), false);

        model.setFactor(hA, bfA);
        model.setFactor(hB, bfB);
        model.setFactor(hC, bfC);

        MinFillOrdering mfo = new MinFillOrdering();
        int[] elimSeq = mfo.apply(model);

        ExpectationMaximization em = new ExpectationMaximization(model, (model, query, observations) -> {
            CutObserved co = new CutObserved();
            GraphicalModel<BayesianFactor> coModel = co.execute(model, observations);

            RemoveBarren rb = new RemoveBarren();
            GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, query, observations);
            rb.filter(elimSeq);

            FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
            fve.setNormalize(true);

            return fve.apply(infModel, query, observations);
        });

        em.execute();
    }

}