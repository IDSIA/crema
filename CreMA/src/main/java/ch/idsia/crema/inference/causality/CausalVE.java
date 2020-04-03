package ch.idsia.crema.inference.causality;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.TIntIntMap;

public class CausalVE extends CausalInference<StructuralCausalModel, BayesianFactor> {


    public CausalVE(StructuralCausalModel model){
        this.model = model;
    }

    @Override
    public BayesianFactor query(int[] target, TIntIntMap evidence, TIntIntMap intervention) {

        // Get the mutilated model
        StructuralCausalModel do_model = applyInterventions(intervention);

        // run variable elimination as usual
        VariableElimination ve = new FactorVariableElimination(do_model.getVariables());
        if(evidence.size()>0) ve.setEvidence(evidence);
        ve.setFactors(do_model.getFactors());
        return (BayesianFactor) ve.run(target);

    }


}
