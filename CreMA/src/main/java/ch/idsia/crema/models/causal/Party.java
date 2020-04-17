package ch.idsia.crema.models.causal;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;


public class Party {
    public static StructuralCausalModel buildModel(){

        // Create an empty model
        StructuralCausalModel model = new StructuralCausalModel();

        // define the variables (endogenous and exogenous)
        int x1 = model.addVariable(2);
        int x2 = model.addVariable(2);
        int x3 = model.addVariable(2);
        int x4 = model.addVariable(2);

        int u1 = model.addVariable(2, true);
        int u2 = model.addVariable(4, true);
        int u3 = model.addVariable(4, true);
        int u4 = model.addVariable(4, true);

        model.addParents(x1, u1);
        model.addParents(x2, u2, x1);
        model.addParents(x3, u3, x1);
        model.addParents(x4, u4, x2, x3);



        // define the factors
        BayesianFactor pu1 = new BayesianFactor(model.getDomain(u1), new double[] { .4, .6 });
        BayesianFactor pu2 = new BayesianFactor(model.getDomain(u2), new double[] { .07, .9, .03, .0 });
        BayesianFactor pu3 = new BayesianFactor(model.getDomain(u3), new double[] { .07, .9, .03, .0 });
        BayesianFactor pu4 = new BayesianFactor(model.getDomain(u4), new double[] { .07, .9, .03, .0 });


/*

        BayesianFactor fx = BayesianFactor.deterministic(model.getDomain(x), model.getDomain(ux), 1,1,0);
        BayesianFactor pux = new BayesianFactor(model.getDomain(ux), new double[] { 0.6, 0.2, 0.2 });

        model.setFactor(x,fx);
        model.setFactor(ux, pux);
*/
        return model;

    }
}
