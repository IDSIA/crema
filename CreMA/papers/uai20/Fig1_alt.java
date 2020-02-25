package uai20;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.RandomUtil;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Fig1_alt {
    public static void main(String[] args) {


        RandomUtil.getRandom().setSeed(23066);


        int x1=0, x2=1;
        int[] endoVarSizes = {2,2};

        SparseDirectedAcyclicGraph dag = new SparseDirectedAcyclicGraph();
        dag.addVariable(x1);
        dag.addVariable(x2);

        dag.addLink(x1, x2);

        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes);
        smodel.fillWithRandomFactors(1);

        int u1 = smodel.getExogenousParents(x1)[0];
        int u2 = smodel.getExogenousParents(x2)[0];


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



        /// Latex

        for(int x: smodel.getEndogenousVars()) {
            BayesianFactor f = smodel.getFactor(x).reorderDomain(smodel.getExogenousParents(x));

            double[][] fdata = ArraysUtil.reshape2d(f.getData(), f.getDomain().getCombinations()/f.getDomain().getCardinality(smodel.getExogenousParents(x)[0]));
            System.out.println(f+" = ");
            //Stream.of(fdata).forEach(d -> System.out.println("\t"+ Arrays.toString(d)));

            System.out.println(ArraysUtil.toLatex(fdata));


        }


    }
}
