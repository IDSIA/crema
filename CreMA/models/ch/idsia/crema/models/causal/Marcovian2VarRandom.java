package ch.idsia.crema.models.causal;

import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;


public class Marcovian2VarRandom {

    public static int PROB_DECIMALS = 1;

    public static StructuralCausalModel buildModel(int[] endoVarSizes, int[] exoVarSizes) {

        int x1=0, x2=1;

        SparseDirectedAcyclicGraph dag = new SparseDirectedAcyclicGraph();
        dag.addVariable(x1);
        dag.addVariable(x2);

        dag.addLink(x1, x2);

        StructuralCausalModel model = new StructuralCausalModel(dag, endoVarSizes, exoVarSizes);

        model.fillWithRandomFactors(PROB_DECIMALS);
        return model;

    }

    public static StructuralCausalModel buildModel() {
        return buildModel(new int[]{2,2}, new int[]{3,5} );
    }
}
