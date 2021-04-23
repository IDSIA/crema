package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.ExtensiveVertexDefaultAlgebra;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ExtensiveVertexFactorTest {

	@Test
	public void test() {
		Strides domain = DomainBuilder.var(1, 2).size(2, 2).strides();
		ExtensiveVertexFactor factor = ExtensiveVertexFactorFactory.factory()
				.domain(domain)
				.addVertex(new double[]{0.1, 0.2, 0.3, 0.4})
				.addVertex(new double[]{0.2, 0.1, 0.2, 0.5})
				.get();
		factor = factor.marginalize(1);

		assertArrayEquals(new double[]{0.3, 0.7}, factor.getInternalVertices().get(0), 1e-9);

		// TODO: not working yet since we have no convex hull
		// assertEquals(1, factor.getInternalVertices().size());
	}

	@Test
	public void test2() {
		Strides domain1 = DomainBuilder.var(0).size(2).strides();
		ExtensiveVertexFactor factor1 = ExtensiveVertexFactorFactory.factory()
				.domain(domain1)
				.addVertex(new double[]{0.1, 0.9})
				.addVertex(new double[]{0.2, 0.8})
				.get();

		Strides domain2 = DomainBuilder.var(0, 1).size(2, 2).strides();
		ExtensiveVertexFactor factor2 = ExtensiveVertexFactorFactory.factory()
				.domain(domain2)
				.addVertex(new double[]{0.1, 0.2, 0.9, 0.8})
				.addVertex(new double[]{0.3, 0.5, 0.7, 0.5})
				.addVertex(new double[]{0.1, 0.5, 0.9, 0.5})
				.addVertex(new double[]{0.3, 0.2, 0.7, 0.8})
				.get();

		VertexFactor factor2s = VertexFactorFactory.factory()
				.domain(Strides.as(1, 2), Strides.as(0, 2))
				.addVertex(new double[]{0.1, 0.9}, 0)
				.addVertex(new double[]{0.2, 0.8}, 1)
				.addVertex(new double[]{0.3, 0.7}, 0)
				.addVertex(new double[]{0.5, 0.5}, 1)
				.build();

		ExtensiveVertexDefaultAlgebra algebra12 = new ExtensiveVertexDefaultAlgebra();
		ExtensiveVertexFactor factor3 = algebra12.combine(factor1, factor2);
		ExtensiveVertexFactor factor4 = algebra12.marginalize(factor3, 0);

		System.out.println(Arrays.toString(factor4.getBayesianVertex(0).getData()));
	}
}
