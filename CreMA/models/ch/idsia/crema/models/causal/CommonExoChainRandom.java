package ch.idsia.crema.models.causal;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalInference;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;


public class CommonExoChainRandom {

    public static int PROB_DECIMALS = 2;

    public static StructuralCausalModel buildModel(int n, int endoSize, int exoSize) {

        StructuralCausalModel model = new StructuralCausalModel();

        // add endogenous
        for (int i=0; i < n; i++) {
            model.addVariable(endoSize);
            if(i>0)
                model.addParent(i, i-1);
        }

        //add exogenous
        for (int i=0; i < n; i+=2) {
            int u = model.addVariable(exoSize, true);
            model.addParent(i,u);
            if(i+1<n) model.addParent(i+1, u);
        }

        System.out.println(model.getNetwork());
        model.fillWithRandomFactors(PROB_DECIMALS);



        return model;

    }

    public static StructuralCausalModel buildModel(int n, int endoSize) {
        return buildModel(n, endoSize, -1);
    }


    public static void main(String[] args) throws InterruptedException {
        int n = 4;
        StructuralCausalModel model = buildModel(n, 2, 5);

        int[] X = model.getEndogenousVars();

        TIntIntHashMap evidence = new TIntIntHashMap();
        evidence.put(X[n-1], 0);

        TIntIntHashMap intervention = new TIntIntHashMap();
        intervention.put(X[0], 0);

        int target = X[1];

        CausalInference inf = new CausalVE(model);
        BayesianFactor result = (BayesianFactor) inf.query(target, evidence, intervention);
        System.out.println(result);



        // error, this is not working
        CausalInference inf2 = new CredalCausalVE(model);
        VertexFactor result2 = (VertexFactor) inf2.query(target, evidence, intervention);
        System.out.println(result2);


        CausalInference inf3 = new CredalCausalAproxLP(model).setEpsilon(0.001);
        IntervalFactor result3 = (IntervalFactor) inf3.query(target, evidence, intervention);
        System.out.println(result3);




    }

}
