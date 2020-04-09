package ch.idsia.crema.models.causal;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalInference;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;


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
            model = new StructuralCausalModel(graph, endo_sizes, exoSize);
        else
            model = new StructuralCausalModel(graph, endo_sizes);


        model.fillWithRandomFactors(PROB_DECIMALS);
        return model;

    }

    public static StructuralCausalModel buildModel(int n, int endoSize) {
        return buildModel(n, endoSize, -1);
    }


    public static void main(String[] args) throws InterruptedException {
        StructuralCausalModel model = buildModel(5, 2, 5);

        int[] X = model.getEndogenousVars();

        // without evidence this is not working
        TIntIntHashMap evidence = new TIntIntHashMap();
        evidence.put(X[X.length-1], 0);

        TIntIntHashMap intervention = new TIntIntHashMap();
        intervention.put(X[0], 0);

        int target = X[2];

        CausalInference inf = new CausalVE(model);
        BayesianFactor result = (BayesianFactor) inf.query(target, evidence, intervention);
        System.out.println(result);

        // error, this is not working
        CausalInference inf2 = new CredalCausalVE(model);
        VertexFactor result2 = (VertexFactor) inf2.query(target, evidence, intervention);
        System.out.println(result2);


        CausalInference inf3 = new CredalCausalAproxLP(model).setEpsilon(0.001);
        IntervalFactor result3 = (IntervalFactor) inf3.query(target, evidence, intervention);
        System.out.println(Arrays.toString(result3.getUpper()));
        System.out.println(Arrays.toString(result3.getLower()));

    }


}
