package examples.docs;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.inference.ve.CredalVariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;




public class inferEx1 {
public static void main(String[] args) throws InterruptedException {

// define the structure
GraphicalModel<VertexFactor> cnet = new DAGModel<>();
int X0 = cnet.addVariable(2);
int X1 = cnet.addVariable(3);
cnet.addParent(X0,X1);
// add credal set K(B)
VertexFactor fb = VertexFactorFactory.factory().domain(cnet.getDomain(X1), Strides.empty())
		.addVertex(new double[]{0.2, 0.5, 0.3})
		.addVertex(new double[]{0.3, 0.4, 0.3})
		.addVertex(new double[]{0.3, 0.2, 0.5})
		.get();
cnet.setFactor(X1,fb);
// add credal set K(A|B)
VertexFactor fa = VertexFactorFactory.factory().domain(cnet.getDomain(X0), cnet.getDomain(X1))
		.addVertex(new double[]{0.5, 0.5}, 0)
		.addVertex(new double[]{0.6, 0.4}, 0)
		.addVertex(new double[]{0.3, 0.7}, 1)
		.addVertex(new double[]{0.4, 0.4}, 1)
		.addVertex(new double[]{0.2, 0.8}, 2)
		.addVertex(new double[]{0.1, 0.9}, 2)
		.get();
cnet.setFactor(X0,fa);
// set up the inference and run the queries
CredalVariableElimination inf = new CredalVariableElimination();
VertexFactor res1 = inf.query(cnet, ObservationBuilder.observe(X0, 0), X1);
VertexFactor res2 = inf.query(cnet, X0);

double[][][] vertices = res1.getData();


}
}
//44