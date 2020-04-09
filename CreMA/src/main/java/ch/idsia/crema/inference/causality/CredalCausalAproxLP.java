package ch.idsia.crema.inference.causality;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.CutObservedSepHalfspace;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CredalCausalAproxLP extends CausalInference<SparseModel, IntervalFactor> {


    private double epsilon = 0.0;

    private StructuralCausalModel originalModel = null;

    public CredalCausalAproxLP(StructuralCausalModel model){

        // Get the empirical and fix the precision problems
        BayesianFactor[] empirical = IntStream.of(model.getEndogenousVars())
                .mapToObj(v -> model.getProb(v).fixPrecission(5,v))
                .toArray(BayesianFactor[]::new);

        this.model = model.toCredalNetwork(false, empirical);
        this.originalModel = model;

    }

    public CredalCausalAproxLP(StructuralCausalModel model, BayesianFactor[] empirical){
        this.model = model.toCredalNetwork(false, empirical);
    }

    @Override
    public IntervalFactor query(int[] target, TIntIntMap evidence, TIntIntMap intervention) throws InterruptedException {

        if(target.length>1)
            throw new IllegalArgumentException("A single target variable is allowed with CredalCausalAproxLP ");

        SparseModel do_csmodel = applyInterventions(intervention);

        // preprocessing
        RemoveBarren removeBarren = new RemoveBarren();
        do_csmodel = removeBarren
                .execute(new CutObservedSepHalfspace().execute(do_csmodel, evidence), target, evidence);


        if(epsilon>0.0){
            for(int v : do_csmodel.getVariables()) {
                //if(originalModel.isExogenous(v))
                    do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getNoised(epsilon));
            }
        }


        // update the evidence
        for(int v: evidence.keys()){
            if(!ArrayUtils.contains(do_csmodel.getVariables(), v)){
                evidence.remove(v);
            }
        }


        IntervalFactor result = null;
        Inference lp1 = new Inference();

        if(evidence.size()>0) {
            int evbin = new BinarizeEvidence().executeInline(do_csmodel, evidence, evidence.size(), false);
            result = lp1.query(do_csmodel, target[0], evbin);
        }else{
            result = lp1.query(do_csmodel, target[0]);
        }

        return result;


    }

    public double getEpsilon() {
        return epsilon;
    }

    public CredalCausalAproxLP  setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }
}
