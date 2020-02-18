package uai20;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.utility.RandomUtil;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Fig2 {
    public static void main(String[] args) {


        RandomUtil.getRandom().setSeed(236226);


        StructuralCausalModel smodel = new StructuralCausalModel();
        int x = smodel.addVariable(2);
        int y = smodel.addVariable(2);
        int u = smodel.addVariable(5,true);

        smodel.addParent(x,u);
        smodel.addParent(y,u);


        BayesianFactor fx = BayesianFactor.deterministic(smodel.getDomain(x), smodel.getDomain(u), 0,0,1,1,1);
        BayesianFactor fy = BayesianFactor.deterministic(smodel.getDomain(y), smodel.getDomain(u), 0,1,0,1,1);

        smodel.setFactor(x,fx);
        smodel.setFactor(y,fy);

        BayesianFactor pu = BayesianFactor.random(smodel.getDomain(u), Strides.empty(),1,false);
        smodel.setFactor(u, pu);

        /////

        StructuralCausalModel this_ = smodel;
        BayesianFactor[] factors = {fx.combine(fy).combine(pu).marginalize(u)};


        SparseModel csmodel = smodel.toVertexNonMarkov(factors);



        smodel.printSummary();

        System.out.println("\nEquivalent credal network");
        System.out.println("=============================");


        for(int v: smodel.getExogenousVars()){
            System.out.println(csmodel.getFactor(v));
        }



        System.out.println("\ndo calculus result:");

        SparseModel do_csmodel = csmodel.intervention(x,0);

        // P(Y|do(x=0)) = P'(Y|x=0) =?= P'(Y)
        VariableElimination ve = new FactorVariableElimination(do_csmodel.getVariables());
        ve.setFactors(do_csmodel.getFactors());
        VertexFactor pdo = (VertexFactor)ve.run(y);
        System.out.println(pdo);

    }
}
