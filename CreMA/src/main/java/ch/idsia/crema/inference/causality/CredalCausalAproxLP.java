package ch.idsia.crema.inference.causality;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObservedSepHalfspace;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.ArrayUtils;

public class CredalCausalAproxLP extends CausalInference<SparseModel, IntervalFactor> {


    private double epsilon = 0.0;

    private StructuralCausalModel originalModel = null;

    public CredalCausalAproxLP(StructuralCausalModel model){

        this.model = model.toCredalNetwork(false, model.getEmpiricalProbs());
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


        //int[] noise = {3,4,5,7,8};
        //int[] noise = {0,1,2,3,4,5,6,7,8};
        //int[] noise = {0,2,4,6,7,8};// N=6
       // int[] noise = {0,1,2,3,4,5,6,7,8,9,10};

        //int[] noise = {0,2,4,7,8,9,10};

/*

        if(true && epsilon>0.0){
            for(int v : noise) {
                //if(originalModel.isExogenous(v))
                //if(do_csmodel.getNetwork())
//                System.out.println(v);
                do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getNoised(epsilon));
   //             do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getNoisedInequalities(epsilon));
  //              do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getNoisedZeroConstraints(epsilon));


            }
        }
*/
//        ((SeparateHalfspaceFactor)do_csmodel.getFactor(1)).printLinearProblem(0);

        if(epsilon>0.0){
            for(int v : do_csmodel.getVariables()) {


                //do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getNoised(epsilon));
                //do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getNoisedInequalities(epsilon));
                do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getPerturbedZeroConstraints(epsilon));
                do_csmodel.setFactor(v, ((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).getPerturbedEqualitiesToOne(epsilon));


                //((SeparateHalfspaceFactor) do_csmodel.getFactor(v)).printLinearProblem();
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
            //ApproxLP2 lp2 = new ApproxLP2();
            //result = lp2.query(do_csmodel, target[0], evidence);

        }else{
            result = lp1.query(do_csmodel, target[0]);
            //ApproxLP2 lp2 = new ApproxLP2();
            //result = lp2.query(do_csmodel, target[0]);
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
