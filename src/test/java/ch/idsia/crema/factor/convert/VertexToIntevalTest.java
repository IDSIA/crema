package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class VertexToIntevalTest {

	@Test
	public void convert() {
		VertexFactor vf = VertexFactorFactory.factory()
				.domain(Strides.var(2, 3), Strides.var(1, 3))
				.addVertex(new double[]{0.2, 0.2, 0.6}, 0)
				.addVertex(new double[]{0.5, 0.2, 0.3}, 0)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 0)

				.addVertex(new double[]{0.2, 0.2, 0.6}, 1)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 1)

				.addVertex(new double[]{0.2, 0.2, 0.6}, 2)
				.get();

		VertexToInterval converter = new VertexToInterval();
		IntervalFactor factor = converter.apply(vf);

		assertArrayEquals(new double[]{0.2, 0.2, 0.3}, factor.getLower(0), 1e-9);
		assertArrayEquals(new double[]{0.5, 0.3, 0.6}, factor.getUpper(0), 1e-9);

		assertArrayEquals(new double[]{0.2, 0.2, 0.4}, factor.getLower(1), 1e-9);
		assertArrayEquals(new double[]{0.3, 0.3, 0.6}, factor.getUpper(1), 1e-9);

		assertArrayEquals(new double[]{0.2, 0.2, 0.6}, factor.getLower(2), 1e-9);
		assertArrayEquals(new double[]{0.2, 0.2, 0.6}, factor.getUpper(2), 1e-9);
	}
}
