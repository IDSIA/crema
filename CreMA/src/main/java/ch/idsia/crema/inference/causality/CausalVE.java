package ch.idsia.crema.inference.causality;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Ints;
import gnu.trove.map.TIntIntMap;

import java.util.stream.IntStream;

public class CausalVE extends CausalInference<StructuralCausalModel, BayesianFactor> {


    private int[] elimOrder;

    public CausalVE(StructuralCausalModel model){
        this.model = model;
        this.elimOrder = model.getVariables();
    }


    @Override
    public BayesianFactor query(int[] target, TIntIntMap evidence, TIntIntMap intervention) {

        // Get the mutilated model
        StructuralCausalModel do_model = applyInterventions(intervention);

        int[] newElimOrder = IntStream.of(elimOrder)
                    .filter(x -> IntStream.of(do_model.getVariables()).anyMatch(i -> i==x)).toArray();

        // run variable elimination as usual
        VariableElimination ve = new FactorVariableElimination(newElimOrder);
        if(evidence.size()>0) ve.setEvidence(evidence);
        ve.setFactors(do_model.getFactors());
        return (BayesianFactor) ve.run(target);

    }

    public int[] getElimOrder() {
        return elimOrder;
    }

    public void setElimOrder(int[] elimOrder) {
        this.elimOrder = elimOrder;
    }
}
