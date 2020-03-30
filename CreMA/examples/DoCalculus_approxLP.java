import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DoCalculus_approxLP {
    public static void main(String[] args) throws InterruptedException {

    // x <- z -> y ;  x -> y

        int x=0, y=1, z=2;
        int[] endoVarSizes = {2,2,2};

        SparseDirectedAcyclicGraph dag = new SparseDirectedAcyclicGraph();

        dag.addVariable(x);
        dag.addVariable(y);
        dag.addVariable(z);

        dag.addLink(x,y);
        dag.addLink(z,x);
        dag.addLink(z,y);

        // Build the causal model
        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes);
        smodel.fillWithRandomFactors(2);


        BayesianFactor[] factors  = IntStream.of(smodel.getEndogenousVars())
                .mapToObj(v -> smodel.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);

        // Get the equivalent model with CS are defined with constraints///
        //
        SparseModel csmodel = smodel.toCredalNetwork(false, factors);

        //SparseModel do_csmodel = csmodel.intervention(x, 0);



        // Get the equivalent model with CS are defined with constraints
        //SparseModel csmodel = smodel.toCredalNetwork(false, factors);


        // Intervention do(x=0)
        SparseModel do_csmodel = csmodel.intervention(x, 0);
        // todo: this should be done at intervention
        int ux = 3;
        do_csmodel.removeVariable(ux);

        // Run inference

        Inference inference = new Inference();
        IntervalFactor res = inference.query(do_csmodel, y);

        System.out.println(Arrays.toString(res.getUpper()));
        System.out.println(Arrays.toString(res.getLower()));






    }
}
