package pgm20.examples;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;

public class Example8 {
    public static void main(String[] args) {

        StructuralCausalModel m = new StructuralCausalModel();

        int x1 = m.addVariable(2);
        int x2 = m.addVariable(2);
        int x3 = m.addVariable(2);
        int u1 = m.addVariable(2, true);
        int u23 = m.addVariable(4, true);

        m.addParents(x3, u23, x2);
        m.addParents(x2, u23, x1);
        m.addParents(x1,u1);

        BayesianFactor p32_1 = new BayesianFactor(m.getDomain(x1,x2,x3),
                new double[]{
                        0.32,
                        0.04,
                        0.32,
                        0.32,
                        0.02,
                        0.67,
                        0.17,
                        0.14,});



        BayesianFactor p1 = new BayesianFactor(m.getDomain(u1),
                new double[]{0.9, 0.1});


        m.setFactor(u1,p1);


    }
}
