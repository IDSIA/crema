import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DoCalculus {
    public static void main(String[] args) {

        // Marcovian case with 3 endogenous variables  P(Y | do(x=0), z=0)
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


        //////// Inputs /////

        StructuralCausalModel model = smodel;

        TIntIntMap evidence = new TIntIntHashMap();
        evidence.put(z, 0);

        TIntIntMap intervention = new TIntIntHashMap();
        intervention.put(x, 0);

        int[] target = {y};

        //////////////////


        // Case 1: CausalVariableElimination

        StructuralCausalModel do_model = model.intervention(intervention.keys()[0], intervention.values()[0]);

        VariableElimination ve = new FactorVariableElimination(do_model.getVariables());
        ve.setFactors(do_model.getFactors());
        ve.setEvidence(evidence);
        BayesianFactor result_cve = (BayesianFactor) ve.run(target);

        System.out.println(Arrays.toString(result_cve.getData()))


        // Case 2: CredalCausalVariableElimination

        BayesianFactor[] factors  = IntStream.of(model.getEndogenousVars())
                .mapToObj(v -> model.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);


        SparseModel csmodel = smodel.toCredalNetwork(true, factors);
        SparseModel do_csmodel = csmodel.intervention(intervention.keys()[0], intervention.values()[0]);


        ve = new FactorVariableElimination(do_csmodel.getVariables());
        ve.setFactors(do_csmodel.getFactors());
        ve.setEvidence(evidence);
        ve.setNormalize(false);
        VertexFactor result_ccve = (VertexFactor) ve.run(target);

        System.out.println(result_ccve)


        // Case 3: CredalCausalApproxLP

        csmodel = smodel.toCredalNetwork(false, factors);
        do_csmodel = csmodel.intervention(intervention.keys()[0], intervention.values()[0]);

        ApproxLP2 lp = new ApproxLP2();
        // IntervalFactor result_ccalp = lp.query(do_csmodel, target[0], evidence) NOT WORKING WITH EVIDENCE
        IntervalFactor result_ccalp = lp.query(do_csmodel, target[0]);
        System.out.println(Arrays.toString(result_ccalp.getUpper()));
        System.out.println(Arrays.toString(result_ccalp.getLower()));




    }
}
