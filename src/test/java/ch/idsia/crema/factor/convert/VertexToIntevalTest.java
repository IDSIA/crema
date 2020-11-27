package ch.idsia.crema.factor.convert;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.core.Strides;

public class VertexToIntevalTest {

	@Test
	public void convert() {
		VertexFactor vf = new VertexFactor(Strides.var(2, 3), Strides.var(1, 3));
		vf.addVertex(new double[] { 0.2, 0.2, 0.6 }, 0);
		vf.addVertex(new double[] { 0.5, 0.2, 0.3 }, 0);
		vf.addVertex(new double[] { 0.3, 0.3, 0.4 }, 0);
		
		vf.addVertex(new double[] { 0.2, 0.2, 0.6 }, 1);
		vf.addVertex(new double[] { 0.3, 0.3, 0.4 }, 1);
		
		vf.addVertex(new double[] { 0.2, 0.2, 0.6 }, 2);
		
		VertexToInterval converter = new VertexToInterval();
		IntervalFactor factor = converter.apply(vf);
		
		assertArrayEquals(new double[]{ 0.2, 0.2, 0.3 }, factor.getLower(0), 0);
		assertArrayEquals(new double[]{ 0.5, 0.3, 0.6 }, factor.getUpper(0), 0);

		assertArrayEquals(new double[]{ 0.2, 0.2, 0.4 }, factor.getLower(1), 0);
		assertArrayEquals(new double[]{ 0.3, 0.3, 0.6 }, factor.getUpper(1), 0);

		assertArrayEquals(new double[]{ 0.2, 0.2, 0.6 }, factor.getLower(2), 0);
		assertArrayEquals(new double[]{ 0.2, 0.2, 0.6 }, factor.getUpper(2), 0);
}
}
