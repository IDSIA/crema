package examples;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;

public class CredalFactors {
	public static void main(String[] args) {

		// Define the domains
		Strides strides_left = DomainBuilder.var(0).size(3).strides();
		Strides strides_right = Strides.empty();

		// define a marginal vertex factor
		VertexFactor f1 = VertexFactorFactory.factory().domain(strides_left, strides_right) // K(vars[0]|[])
				.addVertex(new double[]{0.4, 0.1, 0.5})
				.addVertex(new double[]{0.1, 0.2, 0.2})
				.addVertex(new double[]{0.0, 0.2, 0.4})
				.get();

		// extreme points should sum 1
		f1 = f1.normalize();

		// domains (vertex and separating domains)
		f1.getDataDomain();             //  [0] - [1, 3]
		f1.getSeparatingDomain();       // [] - [1]
		f1.getDataDomain().getClass();  // class ch.idsia.crema.model.Strides


		// define a conditional vertex factor
		strides_left = DomainBuilder.var(1).size(2).strides();
		strides_right = DomainBuilder.var(0).size(3).strides();

		VertexFactor f2 = VertexFactorFactory.factory().domain(strides_left, strides_right) // K(vars[1]|[0])
				// when adding the extreme points, value of the conditioning variables should be specified
				.addVertex(new double[]{0.4, 0.6}, 0)
				.addVertex(new double[]{0.2, 0.8}, 0)

				.addVertex(new double[]{0.3, 0.7}, 1)
				.addVertex(new double[]{0.4, 0.6}, 1)

				.addVertex(new double[]{0.3, 0.7}, 2)
				.addVertex(new double[]{0.4, 0.6}, 2)
				.get();

		f2.getDataDomain();          // [1] - [1, 2]
		f2.getSeparatingDomain();    // [0] - [1, 2]

		f2.getData();
		f2.toString();

		VertexFactor fcombined = f2.combine(f1);

		System.out.println(fcombined);

	}
}
//56