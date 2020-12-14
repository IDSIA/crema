package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.algebra.DefaultExtensiveAlgebra;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class ExtensiveVertexFactorTest {

	@Test
	public void test() {
		Strides domain = DomainBuilder.var(1, 2).size(2, 2).strides();
		ExtensiveVertexFactor factor = new ExtensiveVertexFactor(domain, false);
		factor.addInternalVertex(new double[]{0.1, 0.2, 0.3, 0.4});
		factor.addInternalVertex(new double[]{0.2, 0.1, 0.2, 0.5});
		factor = factor.marginalize(1);

		assertArrayEquals(new double[]{0.3, 0.7}, factor.getInternalVertices().get(0), 0.000000000001);

		// TODO: not working yet since we have no convex hull
		// assertEquals(1, factor.getInternalVertices().size());
	}

	@Test
	public void test2() {
		Strides domain1 = DomainBuilder.var(0).size(2).strides();
		ExtensiveVertexFactor factor1 = new ExtensiveVertexFactor(domain1, false);
		factor1.addVertex(new double[]{0.1, 0.9});
		factor1.addVertex(new double[]{0.2, 0.8});

		Strides domain2 = DomainBuilder.var(0, 1).size(2, 2).strides();
		ExtensiveVertexFactor factor2 = new ExtensiveVertexFactor(domain2, false);
		factor2.addVertex(new double[]{0.1, 0.2, 0.9, 0.8});
		factor2.addVertex(new double[]{0.3, 0.5, 0.7, 0.5});
		factor2.addVertex(new double[]{0.1, 0.5, 0.9, 0.5});
		factor2.addVertex(new double[]{0.3, 0.2, 0.7, 0.8});

		VertexFactor factor2s = new VertexFactor(Strides.as(1, 2), Strides.as(0, 2));
		factor2s.addVertex(new double[]{0.1, 0.9}, 0);
		factor2s.addVertex(new double[]{0.2, 0.8}, 1);
		factor2s.addVertex(new double[]{0.3, 0.7}, 0);
		factor2s.addVertex(new double[]{0.5, 0.5}, 1);

		DefaultExtensiveAlgebra algebra12 = new DefaultExtensiveAlgebra();
		ExtensiveVertexFactor factor3 = algebra12.combine(factor1, factor2);
		ExtensiveVertexFactor factor4 = algebra12.marginalize(factor3, 0);

		System.out.println(Arrays.toString(factor4.getBayesianVertex(0).getData()));
	}
}
