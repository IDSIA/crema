package ch.idsia.crema.inference.causality;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.CutObservedSepHalfspace;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;

import java.util.stream.IntStream;

public class CredalCausalAproxLP extends CausalInference<SparseModel, IntervalFactor> {


    public CredalCausalAproxLP(StructuralCausalModel model){

        // Get the empirical and fix the precision problems
        BayesianFactor[] empirical = IntStream.of(model.getEndogenousVars())
                .mapToObj(v -> model.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);

        this.model = model.toCredalNetwork(false, empirical);

    }

    public CredalCausalAproxLP(StructuralCausalModel model, BayesianFactor[] empirical){
        this.model = model.toCredalNetwork(false, empirical);
    }

    @Override
    public IntervalFactor query(int[] target, TIntIntMap evidence, TIntIntMap intervention) throws InterruptedException {

        if(target.length>1)
            throw new IllegalArgumentException("A single target variable is allowed with CredalCausalAproxLP ");

        SparseModel do_csmodel = applyInterventions(intervention);

        if(evidence.size()>0)
            do_csmodel = new RemoveBarren()
                    .execute(new CutObservedSepHalfspace().execute(do_csmodel, evidence), target, evidence);

        ApproxLP2 lp = new ApproxLP2();
        return lp.query(do_csmodel, target[0]);

    }

}
