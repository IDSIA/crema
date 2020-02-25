import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DoCalculus {
    public static void main(String[] args) {

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

        StructuralCausalModel smodel = new StructuralCausalModel(dag, endoVarSizes);
        smodel.fillWithRandomFactors(2);

        // Conditioning P( Y | x=0)

        VariableElimination ve = new FactorVariableElimination(smodel.getVariables());
        ve.setFactors(smodel.getFactors());
        ve.setEvidence(new TIntIntHashMap(new int[]{x}, new int[]{0}));
        BayesianFactor pcond = (BayesianFactor)ve.run(y);
        System.out.println(Arrays.toString(pcond.getData()));


        // intervention    do(x=0)
        StructuralCausalModel do_model = smodel.intervention(x,0);

        // P(Y|do(x=0)) = P'(Y|x=0) =?= P'(Y)
        ve = new FactorVariableElimination(do_model.getVariables());
        ve.setFactors(do_model.getFactors());
        //ve.setEvidence(new TIntIntHashMap(new int[]{x}, new int[]{0}));
        BayesianFactor pdo = (BayesianFactor)ve.run(y);
        System.out.println(Arrays.toString(pdo.getData()));


        // P(Y|do(x=0)) by operating in the original network
        System.out.println(Arrays.toString(
                smodel.getProb(y).combine(smodel.getProb(z)).marginalize(z).filter(x,0).getData()
        ));


        BayesianFactor[] factors  = IntStream.of(smodel.getEndogenousVars())
                .mapToObj(v -> smodel.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);

        smodel.toVertexSimple(factors);
    }
}
