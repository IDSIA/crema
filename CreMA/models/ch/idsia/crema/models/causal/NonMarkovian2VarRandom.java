package ch.idsia.crema.models.causal;
// models/ch.idsia.crema.models.causal/NonMarkovian2VarRandom.java

import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;


public class NonMarkovian2VarRandom {

    public static int PROB_DECIMALS = 2;

    public static StructuralCausalModel buildModel(int[] endoVarSizes, int exoVarSize) {

        StructuralCausalModel model = new StructuralCausalModel();
        int x = model.addVariable(endoVarSizes[0]);
        int y = model.addVariable(endoVarSizes[1]);
        int u = model.addVariable(exoVarSize,true);

        model.addParent(x,u);
        model.addParent(y,u);
        model.addParent(y,x);

        model.fillWithRandomFactors(PROB_DECIMALS);
        return model;

    }

    public static StructuralCausalModel buildModel() {
        return buildModel(new int[]{2,2});
    }
    public static StructuralCausalModel buildModel(int[] endoVarSizes) {
        return buildModel(endoVarSizes, 5 );
    }
}
