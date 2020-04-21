package pgm20.experiments;

import ch.idsia.crema.models.causal.RandomChainMarkovian;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalInference;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.hash.TIntIntHashMap;

public class ChainMarkovianCase {
    public static void main(String[] args) throws InterruptedException {

        ////////// Parameters //////////

        /** Number of endogenous variables in the chain (should be 3 or greater)*/
        int N = 6;

        /** Number of states in endogenous variables */
        int endoVarSize = 2;

        /** Number of states in the exogenous variables */
        int exoVarSize = 5;

        /** epsilon value for ApproxLP  */
        double eps = 0.0001;

        /////////////////////////////////

        // Load the chain model
        StructuralCausalModel model = RandomChainMarkovian.buildModel(N, endoVarSize, exoVarSize);

        // Query: P(X[N/2] | X[N-1]=0, do(X[0])=0)

        int[] X = model.getEndogenousVars();

        TIntIntHashMap evidence = new TIntIntHashMap();
        evidence.put(X[N-1], 0);

        TIntIntHashMap intervention = new TIntIntHashMap();
        intervention.put(X[0], 0);

        int target = X[N/2];


        // Run inference

        CausalInference inf1 = new CausalVE(model);
        BayesianFactor result1 = (BayesianFactor) inf1.query(target, evidence, intervention);
        System.out.println(result1);

        CausalInference inf2 = new CredalCausalVE(model);
        VertexFactor result2 = (VertexFactor) inf2.query(target, evidence, intervention);
        System.out.println(result2);


        CausalInference inf3 = new CredalCausalAproxLP(model).setEpsilon(eps);
        IntervalFactor result3 = (IntervalFactor) inf3.query(target, evidence, intervention);
        System.out.println(result3);


    }
}
