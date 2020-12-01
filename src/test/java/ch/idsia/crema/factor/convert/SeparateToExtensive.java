package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SeparateToExtensive {
	@Test
	public void testConverter() {
		VertexFactor vf = new VertexFactor(
				DomainBuilder.var(0).size(4).strides(),
				DomainBuilder.var(1,2).size(2,2).strides()
			);
		
		vf.addVertex(new double[] { 0.1, 0.3, 0.2, 0.4 }, 0, 1);
		vf.addVertex(new double[] { 0.3, 0.2, 0.1, 0.4 }, 0, 1);
		vf.addVertex(new double[] { 0.1, 0.3, 0.4, 0.2 }, 0, 1);
		vf.addVertex(new double[] { 0.3, 0.3, 0.2, 0.2 }, 1, 1);
		vf.addVertex(new double[] { 0.6, 0.1, 0.2, 0.1 }, 1, 1);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 1, 1);

		vf.addVertex(new double[] { 0.7, 0.1, 0.1, 0.1 }, 0, 0);
		vf.addVertex(new double[] { 0.2, 0.3, 0.3, 0.2 }, 0, 0);
		vf.addVertex(new double[] { 0.1, 0.1, 0.7, 0.1 }, 1, 0);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 1, 0);
		vf.addVertex(new double[] { 0.3, 0.3, 0.3, 0.1 }, 1, 0);
		vf.addVertex(new double[] { 0.6, 0.1, 0.2, 0.1 }, 1, 0);
		
		SeparateVertexToExtensiveVertex converter = new SeparateVertexToExtensiveVertex();
		ExtensiveVertexFactor evf = converter.apply(vf);
		
		Assert.assertEquals(72, evf.getInternalVertices().size());
		
		BayesianFactor bf = evf.getBayesianVertex(0);
		double sum = 0;
		for (int s = 0; s < 4; ++s) {
			sum += bf.getValue(s, 0, 0);
		}
		
		System.out.println(Arrays.toString(bf.getData()));
		Assert.assertEquals(1, sum, 0.000001);
		
		Assert.assertArrayEquals(new double[] { 0.7, 0.1, 0.1, 0.1,
												0.1, 0.1, 0.7, 0.1, 
												0.1, 0.3, 0.2, 0.4, 
												0.3, 0.3, 0.2, 0.2 }, bf.getData(), 0.000001);
		
	}
}
