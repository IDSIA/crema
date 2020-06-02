package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 14:31
 */
public class ExpectationMaximizationTest {

    GraphicalModel<BayesianFactor> model;

    @Test
    public void testModelLoading() throws InterruptedException {

        model = new SparseModel<>();

        int Pr = model.addVariable(2);
        int Bt = model.addVariable(2);
        int Ut = model.addVariable(2);

        BayesianFactor bfPr = new BayesianFactor(model.getDomain(Pr), false);
        BayesianFactor bfBt = new BayesianFactor(model.getDomain(Pr, Bt), false);
        BayesianFactor bfUt = new BayesianFactor(model.getDomain(Pr, Ut), false);

        bfPr.setData(new double[]{0.5, 0.5});
        bfBt.setData(new double[]{0.5, 0.5, 0.5, 0.5});
        bfUt.setData(new double[]{0.5, 0.5, 0.5, 0.5});

        model.setFactor(Pr, bfPr);
        model.setFactor(Bt, bfBt);
        model.setFactor(Ut, bfUt);

        MinFillOrdering mfo = new MinFillOrdering();
        int[] elimSeq = mfo.apply(model);

        int yes = 0, no = 1, pos = 0, neg = 1;
        TIntIntMap[] observations = new TIntIntMap[]{
                new TIntIntHashMap() {{
                    put(Bt, pos);
                    put(Ut, pos);
                }},
                new TIntIntHashMap() {{
                    put(Pr, yes);
                    put(Bt, neg);
                    put(Ut, pos);
                }},
                new TIntIntHashMap() {{
                    put(Pr, yes);
                    put(Bt, pos);
                }},
                new TIntIntHashMap() {{
                    put(Pr, yes);
                    put(Bt, pos);
                    put(Ut, neg);
                }},
                new TIntIntHashMap() {{
                    put(Bt, neg);
                }},
        };

        ExpectationMaximization em = new ExpectationMaximization(model, (model, query, obs) -> {
            CutObserved co = new CutObserved();
            GraphicalModel<BayesianFactor> coModel = co.execute(model, obs);

            RemoveBarren rb = new RemoveBarren();
            GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, query, obs);
            rb.filter(elimSeq);

            FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
            fve.setNormalize(true);

            return fve.apply(infModel, query, obs);
        }, observations);

        em.execute();
    }

}