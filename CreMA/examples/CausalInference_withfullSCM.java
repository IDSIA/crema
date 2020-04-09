import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.causality.CausalInference;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.CutObservedSepHalfspace;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CausalInference_withfullSCM {
    public static void main(String[] args) throws InterruptedException {

        /**  Marcovian case with 4 endogenous variables:

         INPUT: SCM + evidence + interventions

         x <- z -> y ;  x -> y <- w



         Here, the SCM is completely specified, That is, both, endogenous and exogenous
         variables have their factors associated.

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

        // Build the causal model
        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes);

        // Fill factor with some random valid str. equations and probabilities
        smodel.fillWithRandomFactors(2);

        // Get the exogenous variables for this model
        int ux = smodel.getExogenousParents(x)[0];
        int uy = smodel.getExogenousParents(y)[0];
        int uz = smodel.getExogenousParents(z)[0];
        int uw = smodel.getExogenousParents(w)[0];





        //////// Inputs /////

        StructuralCausalModel model = smodel;   // in cases 2 and 3, empirical probabilities could be given as well

        TIntIntMap evidence = new TIntIntHashMap();
        evidence.put(w, 0);

        TIntIntMap intervention = new TIntIntHashMap();
        intervention.put(x, 0);

        int[] target = {y};

        //////////////////


        // Case 0: operating with the factors

        BayesianFactor result_0 = model.getProb(y).filter(x,0).filter(w,0).combine(model.getProb(z)).marginalize(z);
        System.out.println(Arrays.toString(result_0.getData()));


        // Case 1: CausalVariableElimination

        CausalInference inf1 = new CausalVE(model);
        BayesianFactor result1 = (BayesianFactor) inf1.query(target, evidence, intervention);


        System.out.println(Arrays.toString(result1.getData()));


        // Case 2: CredalCausalVariableElimination

        CausalInference inf2 = new CredalCausalVE(model);
        VertexFactor result2 =  (VertexFactor)inf2.query(target, evidence, intervention);


        System.out.println(result2);

        // Case 3: CredalCausalApproxLP

        CausalInference inf3 = new CredalCausalAproxLP(model);
        IntervalFactor result3 =  (IntervalFactor) inf3.query(target, evidence, intervention);

        System.out.println(Arrays.toString(result3.getUpper()));
        System.out.println(Arrays.toString(result3.getLower()));


    }
}
