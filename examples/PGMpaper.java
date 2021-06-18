package examples;

import ch.idsia.crema.IO;
import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.inference.ve.CredalVariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;

import java.io.IOException;


public class PGMpaper {
	public static void main(String[] args) throws InterruptedException, IOException {

		// define the structure
		DAGModel<VertexFactor> cnet = new DAGModel<>();
		int a = cnet.addVariable(2);
		int b = cnet.addVariable(3);
		cnet.addParent(a, b);
		// create the credal set K(B)
		VertexFactor fb = VertexFactorFactory.factory().domain(cnet.getDomain(b), Strides.empty())
				// specify the extreme points
				.addVertex(new double[]{0.2, 0.5, 0.3})
				.addVertex(new double[]{0.3, 0.4, 0.3})
				.addVertex(new double[]{0.3, 0.2, 0.5})
				.get();
		// attach the factor to the model
		cnet.setFactor(b, fb);
		// create the credal set K(A|B)
		VertexFactor fa = VertexFactorFactory.factory().domain(cnet.getDomain(a), cnet.getDomain(b))
				// specify the extreme points
				.addVertex(new double[]{0.5, 0.5}, 0)
				.addVertex(new double[]{0.6, 0.4}, 0)
				.addVertex(new double[]{0.3, 0.7}, 1)
				.addVertex(new double[]{0.4, 0.4}, 1)
				.addVertex(new double[]{0.2, 0.8}, 2)
				.addVertex(new double[]{0.1, 0.9}, 2)
				.get();
		// attach the factor to the model
		cnet.setFactor(a, fa);

		IO.write(cnet, "./models/pgm-vcredal.uai");


		// set up the inference engine
		CredalVariableElimination inf = new CredalVariableElimination();

		// compute P(B | A = 0)
		VertexFactor res1 = inf.query(cnet, ObservationBuilder.observe(a, 0), b);
		// compute P(B | A = 0)
		VertexFactor res2 = inf.query(cnet, a);


		double[][][] vertices = res1.getData();


	}
}
