import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import org.apache.commons.math3.optim.linear.Relationship;

public class FactorsDef {
    public static void main(String[] args) {



///// code 1 .... LINE 13


// Define the domains
Strides strides_left = DomainBuilder.var(0).size(3).strides();
Strides strides_right = Strides.empty();

double p = 0.2;

// define a marginal vertex factor
VertexFactor f0 = new VertexFactor(strides_left, strides_right);
f0.addVertex(new double[]{p, 0, 1-p});
f0.addVertex(new double[]{0, p, 1-p});



/// code 2


// define a conditional vertex factor
strides_left = DomainBuilder.var(1).size(2).strides();
strides_right = DomainBuilder.var(0).size(3).strides();

VertexFactor f1 = new VertexFactor(strides_left, strides_right); //K(vars[1]|[0])

// when adding the extreme points, value of the conditioning variables should be specified
f1.addVertex(new double[]{0.4, 0.6}, 0);
f1.addVertex(new double[]{0.2, 0.8}, 0);

f1.addVertex(new double[]{0.3, 0.7}, 1);
f1.addVertex(new double[]{0.4, 0.6}, 1);

f1.addVertex(new double[]{0.3, 0.7}, 2);
f1.addVertex(new double[]{0.4, 0.6}, 2);


// code 3

SeparateHalfspaceFactor f0_constr = new SeparateHalfspaceFactor(strides_left, Strides.empty());

// add constraints
f0_constr.addConstraint(new double[]{1., 1., 0.,}, Relationship.EQ, p);
f0_constr.addConstraint(new double[]{0., 0., 1.,}, Relationship.EQ, 1-p);

// normalization constraint
f0_constr.addConstraint(new double[]{1., 1., 1.,}, Relationship.EQ, 1);

// positive constraints
f0_constr.addConstraint(new double[]{1., 0., 0.,}, Relationship.GEQ, 0);
f0_constr.addConstraint(new double[]{0., 1., 0.,}, Relationship.GEQ, 0);
f0_constr.addConstraint(new double[]{0., 0., 1.,}, Relationship.GEQ, 0);



// Define the structure

DAGModel<VertexFactor> cnet = new DAGModel<>();
int X0 = cnet.addVariable(3);
int X1 = cnet.addVariable(2);
cnet.addParent(X1,X0);

// Set the factors
cnet.setFactor(X0, f0);
cnet.setFactor(X1, f1);



    }
}
