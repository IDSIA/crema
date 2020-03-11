import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;

import java.util.Arrays;

public class CredalInference {
    public static void main(String[] args) throws InterruptedException {

        double p = 0.2;
        double eps = 0.0001;


        // Define the model
        SparseModel model = new SparseModel();
        int u = model.addVariable(3);
        int x = model.addVariable(2);
        model.addParent(x,u);

        // Define a credal set of the partent node
        VertexFactor fu = new VertexFactor(model.getDomain(u), Strides.empty());
        fu.addVertex(new double[]{0., 1-p, p-eps});
        fu.addVertex(new double[]{1-p, 0., p});
        model.setFactor(u,fu);


        System.out.println(p+" "+(1-p));

        // Define the credal set of the child
        VertexFactor fx = new VertexFactor(model.getDomain(x), model.getDomain(u));

        fx.addVertex(new double[]{1., 0.,}, 0);
        fx.addVertex(new double[]{1., 0.,}, 1);
        fx.addVertex(new double[]{0., 1.,}, 2);

        model.setFactor(x,fx);

        // Run exact inference inference
        VariableElimination ve = new FactorVariableElimination(model.getVariables());
        ve.setFactors(model.getFactors());
        System.out.println(ve.run(x));


        Inference inference = new Inference();
        double[] upper = inference.query(model, x).getUpper();
        System.out.println(Arrays.toString(upper));         // [NaN, NaN]

        ApproxLP2 approxLP2 = new ApproxLP2();
        upper = inference.query(model, x).getUpper();
        System.out.println(Arrays.toString(upper));         // [NaN, NaN]


        /*
        The same model but with intervals.
        Notes: VE deos not work with these factors.
        BayesianFactor can be added.
        Many problems with numerical stability
         */


        model = new SparseModel();
        x = model.addVariable(2);
        u = model.addVariable(3);
        model.addParent(x,u);

        BayesianFactor ifx = new BayesianFactor(model.getDomain(x,u));
        ifx.setData(new double[] {
                1., 0.,
                1., 0.,
                0., 1.,
        });
        model.setFactor(x, ifx);

        IntervalFactor ifu = new IntervalFactor(model.getDomain(u), model.getDomain());
        ifu.set(new double[] { 0, 0, p-eps}, new double[] { 1-p, 1-p, p });
        model.setFactor(u, ifu);

        inference = new Inference();
        upper = inference.query(model, x).getUpper();
        System.out.println(Arrays.toString(upper));         // [0.8001, 0.19999999999999996]
    }
}
