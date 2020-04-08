package ch.idsia.crema.models.causal;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;


public class SimpleChainRandom {

    public static int PROB_DECIMALS = 2;

    public static StructuralCausalModel buildModel(int n, int endoSize, int exoSize) {

        SparseDirectedAcyclicGraph graph = new SparseDirectedAcyclicGraph();
        int[] endo = new int[n];
        int[] endo_sizes = new int[n];

        for (int i = 0; i < n; i++) {
            endo[i] = i;
            endo_sizes[i] = endoSize;
            graph.addVariable(endo[i]);
            if (i > 0) {
                graph.addLink(endo[i - 1], endo[i]);
            }
        }

        StructuralCausalModel model = null;

        if (exoSize > 0)
            new StructuralCausalModel(graph, endo_sizes, exoSize);
        else
            new StructuralCausalModel(graph, endo_sizes);


        model.fillWithRandomFactors(PROB_DECIMALS);
        return model;

    }

    public static StructuralCausalModel buildModel(int n, int endoSize) {
        return buildModel(n, endoSize, -1);
    }
}
