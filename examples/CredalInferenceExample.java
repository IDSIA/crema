import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.ApproxLP1;
import ch.idsia.crema.inference.approxlp.CredalApproxLP;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.MixedModel;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.Arrays;

public class CredalInferenceExample {
    public static void main(String[] args) throws InterruptedException {

        double p = 0.2;
        double eps = 0.0001;

        /*  CN defined with vertex Factor  */

        // Define the model (with vertex factors)
        final DAGModel<VertexFactor> modelVF = new DAGModel<>();
        int u = modelVF.addVariable(3);
        int x = modelVF.addVariable(2);
        modelVF.addParent(x,u);

        // Define a credal set of the partent node
        VertexFactor fu = new VertexFactor(modelVF.getDomain(u), Strides.empty());
        fu.addVertex(new double[]{0., 1-p, p});
        fu.addVertex(new double[]{1-p, 0., p});
        modelVF.setFactor(u,fu);


        System.out.println(p+" "+(1-p));

        // Define the credal set of the child
        VertexFactor fx = new VertexFactor(modelVF.getDomain(x), modelVF.getDomain(u));

        fx.addVertex(new double[]{1., 0.,}, 0);
        fx.addVertex(new double[]{1., 0.,}, 1);
        fx.addVertex(new double[]{0., 1.,}, 2);

        modelVF.setFactor(x,fx);

        // Run exact inference inference
        VariableElimination<VertexFactor> ve = new FactorVariableElimination<>(modelVF.getVariables());
        ve.setFactors(modelVF.getFactors());
        System.out.println(ve.run(x));

        // AproxLP will not work

        ApproxLP1<VertexFactor> alp1vf = new ApproxLP1<>();
        double[] upper = (alp1vf.query(modelVF, x)).getUpper();
        System.out.println(Arrays.toString(upper));         // [NaN, NaN]

        ApproxLP2<VertexFactor> alp2vf = new ApproxLP2<>();
        upper = (alp2vf.query(modelVF, x)).getUpper();
        System.out.println(Arrays.toString(upper));         // [NaN, NaN]


        /* Interval Factors  */
        // ApproxlP works but with numerical stability problems

        final MixedModel modelMx = new MixedModel();
        x = modelMx.addVariable(2);
        u = modelMx.addVariable(3);
        modelMx.addParent(x,u);

        BayesianFactor ifx = new BayesianFactor(modelMx.getDomain(x,u));
        ifx.setData(new double[] {
                1., 0.,
                1., 0.,
                0., 1.,
        });
        modelMx.setFactor(x, ifx);

        IntervalFactor ifu = new IntervalFactor(modelMx.getDomain(u), modelMx.getDomain());
        ifu.set(new double[] { 0, 0, p-eps}, new double[] { 1-p, 1-p, p });
        modelMx.setFactor(u, ifu);

        ApproxLP2<GenericFactor> alp2Mx = new ApproxLP2<>();
        upper = alp2Mx.query(modelMx, x).getUpper();
        System.out.println(Arrays.toString(upper));         // [0.8001, 0.19999999999999996]



        /* CN with factors specified with constraints  */

        DAGModel<SeparateHalfspaceFactor> modelSH = new DAGModel<>();
        x = modelSH.addVariable(2);
        u = modelSH.addVariable(3);
        modelSH.addParent(x,u);


        SeparateHalfspaceFactor cfu = new SeparateHalfspaceFactor(modelSH.getDomain(u), Strides.empty());

        cfu.addConstraint(new double[]{0,0,1}, Relationship.EQ, p);
        cfu.addConstraint(new double[]{1,1,0}, Relationship.EQ, 1-p);

        cfu.addConstraint(new double[]{1,1,1}, Relationship.EQ, 1);

        cfu.addConstraint(new double[]{1,0,0}, Relationship.GEQ, 0);
        cfu.addConstraint(new double[]{0,1,0}, Relationship.GEQ, 0);
        cfu.addConstraint(new double[]{0,0,1}, Relationship.GEQ, 0);


        HalfspaceToVertex conversor = new HalfspaceToVertex();
        double[][] vertices = conversor.apply(cfu,0).getData()[0];

        SeparateHalfspaceFactor cfx = new SeparateHalfspaceFactor(modelSH.getDomain(x), modelSH.getDomain(u));

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



        modelSH.setFactor(x, cfx);
        modelSH.setFactor(u, cfu);

        conversor.apply(cfx,0).getData();

        CredalApproxLP calp1 = new CredalApproxLP();

        System.out.println(Arrays.toString(calp1.query(modelSH, x).getUpper()));         //
        System.out.println(Arrays.toString(calp1.query(modelSH, x).getLower()));         //

        CredalApproxLP calp2 = new CredalApproxLP();

        System.out.println(Arrays.toString((calp2.query(modelSH, x)).getUpper()));         //
        System.out.println(Arrays.toString((calp2.query(modelSH, x)).getLower()));         //


    }
}
