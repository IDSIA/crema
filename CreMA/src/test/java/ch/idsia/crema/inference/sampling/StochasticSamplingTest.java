package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import org.junit.Before;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public class StochasticSamplingTest {

    @Before
    public void setUp() throws Exception {

        // This model is based on "Modeling and Reasoning with BN", Dawiche, p.378 and following
        SparseModel<BayesianFactor> model = new SparseModel<>();
        BayesianFactor[] f = new BayesianFactor[5];

        // Winter?
        int A = model.addVariable(2);
        f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4}, false);

        // Sprinkler?
        int B = model.addVariable(2);
        model.addParent(B, A);
        f[B] = new BayesianFactor(model.getDomain(B, A), new double[]{.2, .8, .75, .25}, false);

        // Rain?
        int C = model.addVariable(2);
        f[C] = new BayesianFactor(model.getDomain(C, B), new double[]{.8, .2, .1, .9}, false);

        // Wet Grass?
        int D = model.addVariable(2);
        f[D] = new BayesianFactor(model.getDomain(D, B, C), new double[]{.95, .05, .9, .1, .8, .2, 0, 1}, false);

        // Slipepry Road?
        int E = model.addVariable(2);
        f[E] = new BayesianFactor(model.getDomain(E, C), new double[]{.7, .3, 0, 1}, false);

    }
}