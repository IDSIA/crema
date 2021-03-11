import ch.idsia.crema.IO;
import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.approxlp.CredalApproxLP;

import ch.idsia.crema.model.graphical.DAGModel;
import org.apache.commons.math3.optim.linear.Relationship;

import java.io.IOException;


public class PGMpaper2 {
    public static void main(String[] args) throws InterruptedException, IOException {

        // define the structure
        DAGModel cnet = new DAGModel();
        int a = cnet.addVariable(2);
        int b = cnet.addVariable(3);
        cnet.addParent(a, b);

        // add credal set K(B)
        SeparateHalfspaceFactor fb = new SeparateHalfspaceFactor(cnet.getDomain(b), Strides.empty());
        fb.addConstraint(new double[]{1, 0, 0}, Relationship.GEQ, 0.2);
        fb.addConstraint(new double[]{1, 0, 0}, Relationship.LEQ, 0.3);
        fb.addConstraint(new double[]{0, 1, 0}, Relationship.GEQ, 0.4);
        fb.addConstraint(new double[]{0, 1, 0}, Relationship.LEQ, 0.5);
        fb.addConstraint(new double[]{0, 0, 1}, Relationship.GEQ, 0.2);
        fb.addConstraint(new double[]{0, 0, 1}, Relationship.LEQ, 0.3);


        cnet.setFactor(b, fb);

        // add credal set K(A|B)
        SeparateHalfspaceFactor fa = new SeparateHalfspaceFactor(cnet.getDomain(a), cnet.getDomain(b));
        fa.addConstraint(new double[]{1, 0}, Relationship.GEQ, 0.5, 0);
        fa.addConstraint(new double[]{1, 0}, Relationship.LEQ, 0.6, 0);
        fa.addConstraint(new double[]{0, 1}, Relationship.GEQ, 0.4, 0);
        fa.addConstraint(new double[]{0, 1}, Relationship.LEQ, 0.5, 0);
        fa.addConstraint(new double[]{1, 0}, Relationship.GEQ, 0.3, 1);
        fa.addConstraint(new double[]{1, 0}, Relationship.LEQ, 0.4, 1);
        fa.addConstraint(new double[]{0, 1}, Relationship.GEQ, 0.6, 1);
        fa.addConstraint(new double[]{0, 1}, Relationship.LEQ, 0.7, 1);
        fa.addConstraint(new double[]{1, 0}, Relationship.GEQ, 0.1, 2);
        fa.addConstraint(new double[]{1, 0}, Relationship.LEQ, 0.2, 2);
        fa.addConstraint(new double[]{0, 1}, Relationship.GEQ, 0.8, 2);
        fa.addConstraint(new double[]{0, 1}, Relationship.LEQ, 0.9, 2);


        cnet.setFactor(a, fa);


        IO.write(cnet, "./models/pgm-hcredal.uai");

        // set up the inference and run the queries
        Inference inf = new CredalApproxLP();
        IntervalFactor res1 = (IntervalFactor) inf.query(cnet,ObservationBuilder.observe(a, 0), b);
        IntervalFactor res2 = (IntervalFactor) inf.query(cnet, b);

        System.out.println(res1);
        System.out.println(res2);


    }
}
//68
