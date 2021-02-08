import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.Arrays;

public class CredalInferenceExample {

    public static void main(String[] args) throws InterruptedException {


        double p = 0.2;
        double eps = 0.0001;

        /*  CN defined with vertex Factor  */

        // Define the model (with vertex factors)
        DAGModel model = new DAGModel();
        int u = model.addVariable(3);
        int x = model.addVariable(2);
        model.addParent(x,u);

        // Define a credal set of the partent node
        VertexFactor fu = new VertexFactor(model.getDomain(u), Strides.empty());
        fu.addVertex(new double[]{0., 1-p, p});
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

        // AproxLP will not work

        Inference inference = new Inference();
        double[] upper = inference.query(model, x).getUpper();
        System.out.println(Arrays.toString(upper));         // [NaN, NaN]

        ApproxLP2 approxLP2 = new ApproxLP2();
        upper = approxLP2.query(model, x).getUpper();
        System.out.println(Arrays.toString(upper));         // [NaN, NaN]


        /* Interval Factors  */
        // ApproxlP works but with numerical stability problems

        model = new DAGModel();
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



        /* CN with factors specified with constraints  */

        model = new DAGModel();
        x = model.addVariable(2);
        u = model.addVariable(3);
        model.addParent(x,u);


        SeparateHalfspaceFactor cfu = new SeparateHalfspaceFactor(model.getDomain(u), Strides.empty());

        cfu.addConstraint(new double[]{0,0,1}, Relationship.EQ, p);
        cfu.addConstraint(new double[]{1,1,0}, Relationship.EQ, 1-p);

        cfu.addConstraint(new double[]{1,1,1}, Relationship.EQ, 1);

        cfu.addConstraint(new double[]{1,0,0}, Relationship.GEQ, 0);
        cfu.addConstraint(new double[]{0,1,0}, Relationship.GEQ, 0);
        cfu.addConstraint(new double[]{0,0,1}, Relationship.GEQ, 0);


        HalfspaceToVertex conversor = new HalfspaceToVertex();
        double[][] vertices = conversor.apply(cfu,0).getData()[0];

        SeparateHalfspaceFactor cfx = new SeparateHalfspaceFactor(model.getDomain(x), model.getDomain(u));

        cfx.addConstraint(new double[]{1,0}, Relationship.EQ, 1, 0);
        cfx.addConstraint(new double[]{1,0}, Relationship.EQ, 1, 1);
        cfx.addConstraint(new double[]{0,1}, Relationship.EQ, 1, 2);

        cfx.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 0);
        cfx.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 0);

        cfx.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 1);
        cfx.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 1);

        cfx.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 2);
        cfx.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 2);


        cfx.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 0);
        cfx.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 1);
        cfx.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 2);



        model.setFactor(x, cfx);
        model.setFactor(u, cfu);

        conversor.apply(cfx,0).getData();

        inference = new Inference();

        System.out.println(Arrays.toString(inference.query(model, x).getUpper()));         //
        System.out.println(Arrays.toString(inference.query(model, x).getLower()));         //

        approxLP2 = new ApproxLP2();

        System.out.println(Arrays.toString(approxLP2.query(model, x).getUpper()));         //
        System.out.println(Arrays.toString(approxLP2.query(model, x).getLower()));         //


    }
}


