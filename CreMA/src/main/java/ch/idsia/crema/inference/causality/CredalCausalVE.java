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
        if(evidence.size()>0) {
            RemoveBarren removeBarren = new RemoveBarren();
            do_csmodel = removeBarren
                    .execute(new CutObserved().execute(do_csmodel, evidence), target, evidence);

            for(int v : removeBarren.getDeleted())
                evidence.remove(v);

        }

        int[] infvars = do_csmodel.getVariables();
        int[] newElimOrder = ArraysUtil.intersection(elimOrder, infvars);



        FactorVariableElimination ve = new FactorVariableElimination(newElimOrder);
        if(evidence.size()>0)
            ve.setEvidence(evidence);
        ve.setNormalize(false);
        ve.setFactors(do_csmodel.getFactors());
        return ((VertexFactor) ve.run(target)).normalize();

    }


    public void setElimOrder(int[] elimOrder) {
        this.elimOrder = elimOrder;
    }

    public int[] getElimOrder() {
        return elimOrder;
    }
}
