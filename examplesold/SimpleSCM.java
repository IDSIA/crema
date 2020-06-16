import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.BayesianToVertex;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Doubles;

public class SimpleSCM {
    public static void main(String[] args) {
        double p = 0.2;

        // Define the model
        SparseModel model = new SparseModel();
        int u = model.addVariable(4);
        int x = model.addVariable(2);

        model.addParent(x,u);


        // Input fx

        double[][] fx = {  {1.0, 0.0},
                {1.0, 0.0},
                {0.0, 1.0},
                {0.0, 1.0}};

        // Get the credal set of the exogenous variable X
        BayesianFactor cpt_x = new BayesianFactor(model.getDomain(x,u), Doubles.concat(fx));
        VertexFactor kx = new BayesianToVertex().apply(cpt_x,0);



        model.setFactor(x, kx);

        // Get the credal set of the endogenous variable U
        double[][] coeff = ArraysUtil.transpose(fx);
        double[] vals = {p, 1-p};
        VertexFactor ku = new VertexFactor(model.getDomain(u), coeff, vals);
        model.setFactor(u, ku);


        // Run exact inference inference
        VariableElimination ve = new FactorVariableElimination(model.getVariables());
        ve.setFactors(model.getFactors());
        System.out.println(ve.run(x));


    }
}
