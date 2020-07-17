package docs;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.approxlp.CredalApproxLP;
import ch.idsia.crema.model.ObservationBuilder;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import org.apache.commons.math3.optim.linear.Relationship;


public class inferEx2 {
public static void main(String[] args) {

// define the structure
SparseModel cnet = new SparseModel();
int X0 = cnet.addVariable(2);
int X1 = cnet.addVariable(3);
cnet.addParent(X0,X1);

// add credal set K(B)
SeparateHalfspaceFactor fb = new SeparateHalfspaceFactor(cnet.getDomain(X1), Strides.empty());
fb.addConstraint(new double[]{1,0,0}, Relationship.GEQ, 0.2);
fb.addConstraint(new double[]{1,0,0}, Relationship.LEQ, 0.3);
fb.addConstraint(new double[]{0,1,0}, Relationship.GEQ, 0.4);
fb.addConstraint(new double[]{0,1,0}, Relationship.LEQ, 0.5);
fb.addConstraint(new double[]{0,0,1}, Relationship.GEQ, 0.2);
fb.addConstraint(new double[]{0,0,1}, Relationship.LEQ, 0.3);
cnet.setFactor(X1,fb);

// add credal set K(A|B)
SeparateHalfspaceFactor fa = new SeparateHalfspaceFactor(cnet.getDomain(X0), cnet.getDomain(X1));
fa.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.5, 0);
fa.addConstraint(new double[]{1,0}, Relationship.LEQ, 0.6, 0);
fa.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.4, 0);
fa.addConstraint(new double[]{0,1}, Relationship.LEQ, 0.5, 0);
fa.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.3, 1);
fa.addConstraint(new double[]{1,0}, Relationship.LEQ, 0.4, 1);
fa.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.6, 1);
fa.addConstraint(new double[]{0,1}, Relationship.LEQ, 0.7, 1);
fa.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.1, 2);
fa.addConstraint(new double[]{1,0}, Relationship.LEQ, 0.2, 2);
fa.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.8, 2);
fa.addConstraint(new double[]{0,1}, Relationship.LEQ, 0.9, 2);
cnet.setFactor(X0,fa);

// set up the inference and run the queries
Inference inf = new CredalApproxLP(cnet);
IntervalFactor res1 = (IntervalFactor) inf.query(X1, ObservationBuilder.observe(X0, 0));
IntervalFactor res2 = (IntervalFactor) inf.query(X1);

double[] lbound = res1.getLower();
double[] ubound = res1.getUpper();


}
}
