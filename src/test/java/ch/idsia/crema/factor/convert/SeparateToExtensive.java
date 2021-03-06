package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeparateToExtensive {
	@Test
	public void testConverter() {
		VertexFactor vf = VertexFactorFactory.factory()
				.domain(
						DomainBuilder.var(0).size(4).strides(),
						DomainBuilder.var(1, 2).size(2, 2).strides()
				)

				.addVertex(new double[]{0.1, 0.3, 0.2, 0.4}, 0, 1)
				.addVertex(new double[]{0.3, 0.2, 0.1, 0.4}, 0, 1)
				.addVertex(new double[]{0.1, 0.3, 0.4, 0.2}, 0, 1)
				.addVertex(new double[]{0.3, 0.3, 0.2, 0.2}, 1, 1)
				.addVertex(new double[]{0.6, 0.1, 0.2, 0.1}, 1, 1)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 1, 1)

				.addVertex(new double[]{0.7, 0.1, 0.1, 0.1}, 0, 0)
				.addVertex(new double[]{0.2, 0.3, 0.3, 0.2}, 0, 0)
				.addVertex(new double[]{0.1, 0.1, 0.7, 0.1}, 1, 0)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 1, 0)
				.addVertex(new double[]{0.3, 0.3, 0.3, 0.1}, 1, 0)
				.addVertex(new double[]{0.6, 0.1, 0.2, 0.1}, 1, 0)

				.get();

		SeparateVertexToExtensiveVertex converter = new SeparateVertexToExtensiveVertex();
		ExtensiveVertexFactor evf = converter.apply(vf);

		assertEquals(72, evf.getInternalVertices().size());

		BayesianFactor bf = evf.getBayesianVertex(0);
		double sum = 0;
		for (int s = 0; s < 4; ++s) {
			sum += bf.getValue(s, 0, 0);
		}

		System.out.println(Arrays.toString(bf.getData()));
		assertEquals(1, sum, 0.000001);

		assertArrayEquals(new double[]{0.7, 0.1, 0.1, 0.1,
				0.1, 0.1, 0.7, 0.1,
				0.1, 0.3, 0.2, 0.4,
				0.3, 0.3, 0.2, 0.2}, bf.getData(), 0.000001);

	}
}
