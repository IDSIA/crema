import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.credici.model.StructuralCausalModel;

import java.util.stream.IntStream;

public class ToVertexAndConstraints {
    public static void main(String[] args) {

        // This is a markovian case, but should work for a non markovian

        int x1=0, x2=1;
        int[] endoVarSizes = {2,2};

        SparseDirectedAcyclicGraph dag = new SparseDirectedAcyclicGraph();
        dag.addVariable(x1);
        dag.addVariable(x2);
        dag.addLink(x1, x2);

        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes, 3,5);
        smodel.fillWithRandomFactors(1);

        int u1 = smodel.getExogenousParents(x1)[0];
        int u2 = smodel.getExogenousParents(x2)[0];


        // set functions as given in the paper
        BayesianFactor[] empirical  = IntStream.of(smodel.getEndogenousVars())
                .mapToObj(v -> smodel.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);


        // model with constraints
        SparseModel csmodel_const = smodel.toCredalNetwork(false, empirical);

        // model with vertices (vertex flag is ture by default)
        SparseModel csmodel_vertex = smodel.toCredalNetwork(empirical);


        // print the models
        smodel.printSummary();

        System.out.println("\nEquivalent credal network");
        System.out.println("=============================");

        for(int v: smodel.getVariables()){
            SeparateHalfspaceFactor f = ((SeparateHalfspaceFactor)csmodel_const.getFactor(v));
            System.out.println(f.getDataDomain());
            for(int i=0; i<f.getSeparatingDomain().getCombinations(); i++)
                f.printLinearProblem(i);
            System.out.println("");
            System.out.println("");

        }


        System.out.println("\nEquivalent credal network with vertices");
        System.out.println("=============================");

        for(int v: smodel.getVariables()){
            System.out.println(csmodel_vertex.getFactor(v));
            csmodel_vertex.getFactor(v);
        }

    }

}
