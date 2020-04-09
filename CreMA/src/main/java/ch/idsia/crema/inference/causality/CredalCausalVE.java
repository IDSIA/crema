package ch.idsia.crema.inference.causality;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.IntStream;

public class CredalCausalVE extends CausalInference<SparseModel, VertexFactor> {

    private int[] elimOrder;

    public CredalCausalVE(StructuralCausalModel model){
        // Get the empirical and fix the precision problems
        BayesianFactor[] empirical = IntStream.of(model.getEndogenousVars())
                .mapToObj(v -> model.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);

        this.model = model.toCredalNetwork(true, empirical);
        this.elimOrder = this.model.getVariables();
    }


    public CredalCausalVE(StructuralCausalModel model, BayesianFactor[] empirical){
        this.model = model.toCredalNetwork(true, empirical);
    }


    @Override
    public VertexFactor query(int[] target, TIntIntMap evidence, TIntIntMap intervention) {

        SparseModel do_csmodel = applyInterventions(intervention);

    // cut arcs coming from an observed node and remove barren w.r.t the target
        RemoveBarren removeBarren = new RemoveBarren();
        do_csmodel = removeBarren
                .execute(new CutObserved().execute(do_csmodel, evidence), target, evidence);

        // update the evidence
        for(int v: evidence.keys()){
            if(!ArrayUtils.contains(do_csmodel.getVariables(), v)){
                evidence.remove(v);
            }
        }

        // Get the new elimination order
        int[] newElimOrder = ArraysUtil.intersection(elimOrder, do_csmodel.getVariables());


        FactorVariableElimination ve = new FactorVariableElimination(newElimOrder);
        if(evidence.size()>0)
            ve.setEvidence(evidence);
        ve.setNormalize(false);
        VertexFactor.CONVEX_HULL_MARG = true;
        ve.setFactors(do_csmodel.getFactors());
        return ((VertexFactor) ve.run(target)).normalize();

    }


    public CredalCausalVE setElimOrder(int[] elimOrder) {
        this.elimOrder = elimOrder;
        return this;
    }

    public int[] getElimOrder() {
        return elimOrder;
    }
}
