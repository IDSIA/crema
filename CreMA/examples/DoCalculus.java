import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;

public class DoCalculus {
    public static void main(String[] args) {

    // x <- z -> y ;  x -> y

        BayesianNetwork emodel = new BayesianNetwork();

        int x = emodel.addVariable(2);
        int y = emodel.addVariable(2);
        int z = emodel.addVariable(2);

        emodel.addParents(y,x,z);
        emodel.addParent(x,z);

        StructuralCausalModel smodel = StructuralCausalModel.getCausalStructFromBN(emodel, 5);
        smodel.fillWithRandomFactors(2);

        // Conditioning P( Y | x=0)

        VariableElimination ve = new FactorVariableElimination(smodel.getVariables());
        ve.setFactors(smodel.getFactors());
        ve.setEvidence(new TIntIntHashMap(new int[]{x}, new int[]{0}));
        BayesianFactor pcond = (BayesianFactor)ve.run(y);
        System.out.println(Arrays.toString(pcond.getData()));


        // intervention    do(x=0)
        StructuralCausalModel do_model = smodel.intervention(x,0);

        // P(Y|do(x=0)) = P'(Y|x=0) =?= P'(Y)
        ve = new FactorVariableElimination(do_model.getVariables());
        ve.setFactors(do_model.getFactors());
        //ve.setEvidence(new TIntIntHashMap(new int[]{x}, new int[]{0}));
        BayesianFactor pdo = (BayesianFactor)ve.run(y);
        System.out.println(Arrays.toString(pdo.getData()));


        // P(Y|do(x=0)) by operating in the original network
        smodel.getProb(y).combine(smodel.getProb(z)).marginalize(z).filter(x,1).getData();
    }
}
