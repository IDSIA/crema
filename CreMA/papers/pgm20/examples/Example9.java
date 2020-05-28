package pgm20.examples;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.models.causal.Party;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Example9 {
    public static void main(String[] args) throws InterruptedException {



        StructuralCausalModel m = Party.buildModel();

        int x1 = 0, x2 = 1, x3=2, x4=3;
        int u1 = 4, u2=5, u3=6, u4=3;


        // Saves the true factors
        SparseModel model = m.toCredalNetwork(m.getEmpiricalProbs());

        int x1_ = model.addVariable(2);
        int x2_ = model.addVariable(2);
        int x3_ = model.addVariable(2);
        int x4_ = model.addVariable(2);

        model.addParents(x1_, u1);
        model.addParents(x2_, u2, x1_);
        model.addParents(x3_, u3, x1_);
        model.addParents(x4_, u4, x2_, x3_);


        int[] X = {x1,x2,x3,x4};
        int[] U = {u1,u2,u3,u4};
        int[] X_ = {x1_,x2_,x3_,x4_};

        int diff = X_[0]-X[0];

        for(int i=0; i<X.length; i++){
            VertexFactor fx = (VertexFactor)model.getFactor(X[i]);

            int[] newvars_l = IntStream.of(fx.getDataDomain().getVariables()).map(v -> {if(v<4) return v+diff; else return v;}).toArray();

            int[] newvars_r = IntStream.of(fx.getSeparatingDomain().getVariables()).map(v -> {if(v<4) return v+diff; else return v;}).toArray();

            System.out.println(Arrays.toString(newvars_l));
            System.out.println(Arrays.toString(newvars_r));

            model.setFactor(X_[i], new VertexFactor(model.getDomain(newvars_l), model.getDomain(newvars_r), fx.getData()));
        }


        SparseModel do_csmodel = model.intervention(x2_,1);

        TIntIntHashMap evidence = new TIntIntHashMap();
        evidence.put(x2, 0);
        int target = x4_;


        // cut arcs coming from an observed node and remove barren w.r.t the target
        RemoveBarren removeBarren = new RemoveBarren();
        do_csmodel = removeBarren
                .execute(new CutObserved().execute(do_csmodel, evidence), target, evidence);

        System.out.println(Arrays.toString(removeBarren.getDeleted()));

        TIntIntHashMap filteredEvidence = new TIntIntHashMap();
        // update the evidence
        for(int v: evidence.keys()){
            if(ArrayUtils.contains(do_csmodel.getVariables(), v)){
                filteredEvidence.put(v, evidence.get(v));
            }
        }

        // Get the new elimination order
        int[] newElimOrder = do_csmodel.getVariables();

        System.out.println("deleted = "+ Arrays.toString(removeBarren.getDeleted()));
        FactorVariableElimination ve = new FactorVariableElimination(newElimOrder);
        if(filteredEvidence.size()>0)
            ve.setEvidence(filteredEvidence);
        ve.setNormalize(false);
        VertexFactor.CONVEX_HULL_MARG = true;
        ve.setFactors(do_csmodel.getFactors());
        VertexFactor results = ((VertexFactor) ve.run(target)).normalize().convexHull(true);

        System.out.println(results);


    }
}
