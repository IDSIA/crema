package uai20;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.utility.RandomUtil;

import java.util.stream.IntStream;

public class Fig1 {
    public static void main(String[] args) {


        RandomUtil.getRandom().setSeed(23066);


        int x1=0, x2=1, x3=2;
        int[] endoVarSizes = {2,2,2};

        SparseDirectedAcyclicGraph dag = new SparseDirectedAcyclicGraph();
        dag.addVariable(x1);
        dag.addVariable(x2);
        dag.addVariable(x3);

        dag.addLink(x1, x3);
        dag.addLink(x2,x3);

        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes);
        smodel.fillWithRandomFactors(1);


        // set functions as given in the paper
        BayesianFactor[] empirical  = IntStream.of(smodel.getEndogenousVars())
                .mapToObj(v -> smodel.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);



        SparseModel csmodel = smodel.toVertexSimple(empirical);


        smodel.printSummary();

        System.out.println("\nEquivalent credal network");
        System.out.println("=============================");


        for(int v: smodel.getExogenousVars()){
            System.out.println(csmodel.getFactor(v));
        }



    }
}
