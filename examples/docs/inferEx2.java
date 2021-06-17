package examples.docs;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorFactory;
import ch.idsia.crema.inference.approxlp.CredalApproxLP;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import org.apache.commons.math3.optim.linear.Relationship;



public class inferEx2 {
public static void main(String[] args) throws InterruptedException {

// define the structure
GraphicalModel<SeparateHalfspaceFactor> cnet = new DAGModel<>();
int X0 = cnet.addVariable(2);
int X1 = cnet.addVariable(3);
cnet.addParent(X0,X1);

// add credal set K(B)
SeparateHalfspaceFactor fb = SeparateHalfspaceFactorFactory.factory().domain(cnet.getDomain(X1), Strides.empty())
		.constraint(new double[]{1,0,0}, Relationship.GEQ, 0.2)
		.constraint(new double[]{1,0,0}, Relationship.LEQ, 0.3)
		.constraint(new double[]{0,1,0}, Relationship.GEQ, 0.4)
		.constraint(new double[]{0,1,0}, Relationship.LEQ, 0.5)
		.constraint(new double[]{0,0,1}, Relationship.GEQ, 0.2)
		.constraint(new double[]{0,0,1}, Relationship.LEQ, 0.3)
		.get();
cnet.setFactor(X1,fb);

// add credal set K(A|B)
SeparateHalfspaceFactor fa = SeparateHalfspaceFactorFactory.factory().domain(cnet.getDomain(X0), cnet.getDomain(X1))
		.constraint(new double[]{1,0}, Relationship.GEQ, 0.5, 0)
		.constraint(new double[]{1,0}, Relationship.LEQ, 0.6, 0)
		.constraint(new double[]{0,1}, Relationship.GEQ, 0.4, 0)
		.constraint(new double[]{0,1}, Relationship.LEQ, 0.5, 0)
		.constraint(new double[]{1,0}, Relationship.GEQ, 0.3, 1)
		.constraint(new double[]{1,0}, Relationship.LEQ, 0.4, 1)
		.constraint(new double[]{0,1}, Relationship.GEQ, 0.6, 1)
		.constraint(new double[]{0,1}, Relationship.LEQ, 0.7, 1)
		.constraint(new double[]{1,0}, Relationship.GEQ, 0.1, 2)
		.constraint(new double[]{1,0}, Relationship.LEQ, 0.2, 2)
		.constraint(new double[]{0,1}, Relationship.GEQ, 0.8, 2)
		.constraint(new double[]{0,1}, Relationship.LEQ, 0.9, 2)
		.get();
cnet.setFactor(X0,fa);

// set up the inference and run the queries
CredalApproxLP<SeparateHalfspaceFactor> inf = new CredalApproxLP<>();
IntervalFactor res1 = inf.query(cnet, ObservationBuilder.observe(X0, 0), X1);
IntervalFactor res2 = inf.query(cnet, X1);

double[] lbound = res1.getLower();
double[] ubound = res1.getUpper();


}
}
//58