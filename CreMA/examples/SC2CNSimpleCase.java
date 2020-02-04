import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.ArrayUtils;

public class SC2CNSimpleCase {
    public static void main(String[] args) {


        BayesianNetwork emodel = new BayesianNetwork();

        int x = emodel.addVariable(2);
        int y = emodel.addVariable(2);
        int z = emodel.addVariable(2);

        emodel.addParents(y,x,z);

        emodel.setFactor(x, new BayesianFactor(emodel.getDomain(x), new double[] {0.2, 0.8}));
        emodel.setFactor(z, new BayesianFactor(emodel.getDomain(z), new double[] {0.4, 0.6}));

        emodel.setFactor(y, new BayesianFactor(
                emodel.getDomain(y),
                new double[] {0.5, 0.5, 0.6, 0.4, 0.9, 0.1, 0.3, 0.7}
        ));

        // define the SM

        StructuralCausalModel smodel = StructuralCausalModel.getSimpleSCMfromBN(emodel, 5);

        double[][] fx = {  {1.0, 0.0},
                {1.0, 0.0},
                {0.0, 1.0},
                {0.0, 1.0},
                {0.0, 1.0}};


        double[][] fz = {   {1.0, 0.0},
                {0.0, 1.0},
                {0.0, 1.0},
                {0.0, 1.0},
                {1.0, 0.0}};


        double[][] fy = {   {1.0, 0.0,  0.0, 1.0,   1.0, 0.0,   1.0, 0.0},
                {0.0, 1.0,  0.0, 1.0,   1.0, 0.0,   0.0, 1.0},
                {0.0, 1.0,  1.0, 0.0,   0.0, 1.0,   1.0, 0.0},
                {0.0, 1.0,  1.0, 0.0,   1.0, 0.0,   0.0, 1.0},
                {1.0, 0.0,  1.0, 0.0,   1.0, 0.0,   0.0, 1.0},
        };



        smodel.setFactor(x,new BayesianFactor(
                smodel.getDomain(ArrayUtils.add(smodel.getParents(x), 0, x)), Doubles.concat(fx)));

        smodel.setFactor(y,new BayesianFactor(
                smodel.getDomain(ArrayUtils.add(smodel.getParents(y), 0, y)), Doubles.concat(fy)));

        smodel.setFactor(z,new BayesianFactor(
                smodel.getDomain(ArrayUtils.add(smodel.getParents(z), 0, z)), Doubles.concat(fz)));


        // Inputs: emodel + smodel

        SparseModel cmodel = smodel.toVertexSimple(emodel);

        //////// Heap space problems
    /* Run exact inference inference
    VariableElimination ve = new FactorVariableElimination(cmodel.getVariables());
    ve.setFactors(cmodel.getFactors());
    System.out.println(ve.run(y));
    */

        int ux = cmodel.getParents(x)[0];
        int uz = cmodel.getParents(z)[0];
        int uy = cmodel.getParents(y)[2];


        VertexFactor kux =  (VertexFactor)cmodel.getFactor(ux);
        VertexFactor kuy =  (VertexFactor)cmodel.getFactor(uy);
        VertexFactor kuz =  (VertexFactor)cmodel.getFactor(uz);

        VertexFactor kx =  (VertexFactor)cmodel.getFactor(x);
        VertexFactor ky =  (VertexFactor)cmodel.getFactor(y);
        VertexFactor kz =  (VertexFactor)cmodel.getFactor(z);


        // These should be equal to the empirical probabilities
        System.out.println(kux.combine(kx).marginalize(ux));
        System.out.println(kuz.combine(kz).marginalize(uz));
        System.out.println(kuy.combine(ky).marginalize(uy));

    }
}
