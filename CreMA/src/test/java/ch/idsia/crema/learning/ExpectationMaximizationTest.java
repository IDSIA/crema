package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 14:31
 */
public class ExpectationMaximizationTest {

    SparseModel<BayesianFactor> model;

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

        ExpectationMaximization<BayesianFactor> em = new ExpectationMaximization<>(model);

        em.execute();
    }

}