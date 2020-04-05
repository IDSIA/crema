import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalInference;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CausalInference_withEmpirical {
    public static void main(String[] args) throws InterruptedException {



        /**  Marcovian case with 4 endogenous variables:

         INPUT: SCM + empirical probs. + evidence + interventions

                x <- z -> y ;  x -> y <- w

         Here the empirical probabilities are specified separated to the model.
         Thus, exogenous variables do not have associated any factor. By contrast,
         endogenous variables have their structural equations.

        */


        int x=0, y=1, z=2, w=3;
        int[] endoVarSizes = {2,2,2,2};
        SparseDirectedAcyclicGraph dag = new SparseDirectedAcyclicGraph();
        dag.addVariable(x);
        dag.addVariable(y);
        dag.addVariable(z);
        dag.addVariable(w);
        dag.addLink(x,y);
        dag.addLink(z,x);
        dag.addLink(z,y);
        dag.addLink(w,y);

        // Build the causal model (markovian case)
        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes);

        int ux = smodel.getExogenousParents(x)[0];
        int uy = smodel.getExogenousParents(y)[0];
        int uz = smodel.getExogenousParents(z)[0];
        int uw = smodel.getExogenousParents(w)[0];


        // Get a valid specification of the model (empirical probs + equations)
        TIntObjectMap[] spec = smodel.getRandomFactors(2);
        TIntObjectMap empiricalMap = spec[0];
        TIntObjectMap structEquMap = spec[1];


        // Set the equations to the model
        for(int v : smodel.getEndogenousVars()){
            smodel.setFactor(v, (BayesianFactor) structEquMap.get(v));
        }


        //////// Inputs /////

        StructuralCausalModel model = smodel;

        BayesianFactor[] empirical = IntStream.of(empiricalMap.keys())
                .mapToObj(v -> empiricalMap.get(v))
                .toArray(BayesianFactor[]::new);

        TIntIntMap evidence = new TIntIntHashMap();
        evidence.put(w, 0);

        TIntIntMap intervention = new TIntIntHashMap();
        intervention.put(x, 0);

        int[] target = {y};

        //////////////////


        // Case 1: CausalVariableElimination
        /*
         This case has not sense without a complete specification fo the SCM

        CausalInference inf1 = new CausalVE(model);
        BayesianFactor result1 = (BayesianFactor) inf1.query(target, evidence, intervention);


        System.out.println(Arrays.toString(result1.getData()));

         */


        // Case 2: CredalCausalVariableElimination

        CausalInference inf2 = new CredalCausalVE(model, empirical);
        VertexFactor result2 =  (VertexFactor)inf2.query(target, evidence, intervention);


        System.out.println(result2);

        // Case 3: CredalCausalApproxLP

        CausalInference inf3 = new CredalCausalAproxLP(model, empirical);
        IntervalFactor result3 =  (IntervalFactor) inf3.query(target, evidence, intervention);

        System.out.println(Arrays.toString(result3.getUpper()));
        System.out.println(Arrays.toString(result3.getLower()));

    }
}
