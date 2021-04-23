package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestVertexFactor {

	@Test
	public void testFilterLeft() {
		DAGModel<IntervalFactor> model = new DAGModel<>();
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		VertexFactor factorx = VertexFactorFactory.factory().domain(model.getDomain(A, B), model.getDomain(C))

				// A0B0 A1B0 A0B1 A1B1
				.addVertex(new double[]{0.3, 0.5, 0.1, 0.1}, 1)
				.addVertex(new double[]{0.5, 0.2, 0.2, 0.1}, 1)
				.addVertex(new double[]{0.1, 0.2, 0.4, 0.3}, 0)
				.addVertex(new double[]{0.1, 0.3, 0.2, 0.2}, 0)
				.build();

		factorx = factorx.filter(A, 1).filter(B, 0);
		double[][] x = factorx.getVertices(0);
		assertEquals(2, x.length);
		assertArrayEquals(new double[]{0.2}, x[0], .0000001);
		assertArrayEquals(new double[]{0.3}, x[1], .0000001);

		x = factorx.getVertices(1);
		assertEquals(2, x.length);
		assertArrayEquals(new double[]{0.5}, x[0], .0000001);
		assertArrayEquals(new double[]{0.2}, x[1], .0000001);
	}

	@Test
	public void testLeftFilter2() {
		DAGModel<IntervalFactor> model = new DAGModel<>();
		int A = model.addVariable(2);
		int B = model.addVariable(3);
		int C = model.addVariable(2);

		VertexFactor factorx = VertexFactorFactory.factory().domain(model.getDomain(A, B, C), model.getDomain())
				.addVertex(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
				.build();
		VertexFactor v1 = factorx.filter(B, 1);
		VertexFactor v2 = factorx.filter(B, 2);
		VertexFactor v3 = factorx.filter(C, 1);
		VertexFactor v4 = factorx.filter(A, 1);

		double[][] x1 = v1.getVertices();
		double[][] x2 = v2.getVertices();
		double[][] x3 = v3.getVertices();
		double[][] x4 = v4.getVertices();

		// all must be 1 to multiply to 1
		assertEquals(1, x1.length * x2.length * x3.length * x4.length);

		assertArrayEquals(new double[]{3, 4, 9, 10}, x1[0], 0.00000001);
		assertArrayEquals(new double[]{5, 6, 11, 12}, x2[0], 0.00000001);

		assertArrayEquals(new double[]{7, 8, 9, 10, 11, 12}, x3[0], 0.00000001);
		assertArrayEquals(new double[]{2, 4, 6, 8, 10, 12}, x4[0], 0.00000001);

	}

	@Test
	public void testAdd() {
		VertexFactor vf = VertexFactorFactory.factory().domain(Strides.as(1, 4), Strides.as(0, 3).and(5, 2))
				.addVertex(new double[]{0.1, 0.3, 0.2, 0.4}, 2, 1)
				.build();
		assertArrayEquals(new double[]{0.1, 0.3, 0.2, 0.4}, vf.getVertices(2, 1)[0], 0.000001);
		assertNull(vf.getVertices(0, 1));
	}

	@Test
	public void testExpansion() {
		// p(X1|X0,X5)
		VertexFactor vf = VertexFactorFactory.factory().domain(Strides.as(1, 4), Strides.as(0, 3).and(5, 2))
				.addVertex(new double[]{0.1, 0.3, 0.2, 0.4}, 0, 1)
				.addVertex(new double[]{0.3, 0.2, 0.1, 0.4}, 0, 1)
				.addVertex(new double[]{0.1, 0.3, 0.4, 0.2}, 0, 1)
				.addVertex(new double[]{0.3, 0.3, 0.2, 0.2}, 1, 1)
				.addVertex(new double[]{0.6, 0.1, 0.2, 0.1}, 2, 1)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 2, 1)

				.addVertex(new double[]{0.7, 0.1, 0.1, 0.1}, 0, 0)
				.addVertex(new double[]{0.2, 0.3, 0.3, 0.2}, 0, 0)
				.addVertex(new double[]{0.1, 0.1, 0.7, 0.1}, 1, 0)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 1, 0)
				.addVertex(new double[]{0.3, 0.3, 0.3, 0.1}, 1, 0)
				.addVertex(new double[]{0.6, 0.1, 0.2, 0.1}, 2, 0)
				.addVertex(new double[]{0.1, 0.2, 0.2, 0.5}, 2, 0)
				.build();

		// P(X1|X0,X5) -> diventa estensivo rispetto a X5
		VertexFactor v2 = vf.reseparate(Strides.as(0, 3));
		double[][][] dta = v2.getData();
		assertEquals(3, dta.length);

		// we removed var 5 from the separation domain
		assertEquals(6, dta[0].length);
		assertEquals(3, dta[1].length);
		assertEquals(4, dta[2].length);
		assertEquals(8, dta[2][0].length);

		// test some vertices
		assertArrayEquals(new double[]{0.7, 0.1, 0.1, 0.1, 0.1, 0.3, 0.2, 0.4}, dta[0][0], 0.00001);
		assertArrayEquals(new double[]{0.2, 0.3, 0.3, 0.2, 0.1, 0.3, 0.2, 0.4}, dta[0][1], 0.00001);
		assertArrayEquals(new double[]{0.2, 0.3, 0.3, 0.2, 0.3, 0.2, 0.1, 0.4}, dta[0][3], 0.00001);
		assertArrayEquals(new double[]{0.4, 0.2, 0.2, 0.2, 0.3, 0.3, 0.2, 0.2}, dta[1][1], 0.00001);
		assertArrayEquals(new double[]{0.6, 0.1, 0.2, 0.1, 0.4, 0.2, 0.2, 0.2}, dta[2][2], 0.00001);

		v2 = vf.reseparate(Strides.as(5, 2));
		dta = v2.getData();
		assertEquals(2, dta.length);

		// we removed var 0 from the separation domain
		assertEquals(12, dta[0].length);
		assertEquals(6, dta[1].length);
		assertEquals(12, dta[1][0].length);

		// test some vertices
		assertArrayEquals(new double[]{0.7, 0.1, 0.6, 0.1, 0.1, 0.1, 0.1, 0.7, 0.2, 0.1, 0.1, 0.1}, dta[0][0],
				0.00001);
		assertArrayEquals(new double[]{0.7, 0.1, 0.6, 0.1, 0.1, 0.1, 0.1, 0.7, 0.2, 0.1, 0.1, 0.1}, dta[0][0],
				0.00001);

		// no reseparation!

		VertexFactor vf1_05 = VertexFactorFactory.factory().domain(Strides.as(1, 2), Strides.as(0, 2, 5, 2))
				.addVertex(new double[]{0.3, 0.7}, 0, 0)
				.addVertex(new double[]{0.4, 0.6}, 0, 1)
				.addVertex(new double[]{0.7, 0.3}, 0, 1)
				.addVertex(new double[]{0.9, 0.1}, 1, 0)
				.addVertex(new double[]{0.6, 0.4}, 1, 0)
				.addVertex(new double[]{0.5, 0.5}, 1, 1)
				.addVertex(new double[]{0.9, 0.1}, 1, 1)
				.build();

		v2 = vf1_05.reseparate(vf1_05.getSeparatingDomain());
		int s = 0;

		double[][][] source = vf1_05.getData(), destination = v2.getData();
		assertEquals(source.length, destination.length);
		for (; s < source.length; ++s) {
			assertEquals(source[s].length, destination[s].length);
			for (int v = 0; v < source[s].length; ++v) {
				assertArrayEquals(source[s][v], destination[s][v], 1e-7);
			}
		}

	}

	@Test
	public void testMarginalize() {
		VertexFactor vf = VertexFactorFactory.factory().domain(Strides.as(1, 2, 2, 2), Strides.as(0, 2))
				.addVertex(new double[]{0.6, 0.2, 0.1, 0.1}, 0)
				.addVertex(new double[]{0.2, 0.3, 0.4, 0.1}, 0)
				.addVertex(new double[]{0.1, 0.1, 0.7, 0.1}, 1)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 1)
				.addVertex(new double[]{0.3, 0.3, 0.3, 0.1}, 1)
				.build();

		VertexFactor v2 = vf.marginalize(2);
		double[][] v = v2.getVertices(0);
		assertArrayEquals(new double[]{0.7, 0.3}, v[0], 0.000001);
		assertArrayEquals(new double[]{0.6, 0.4}, v[1], 0.000001);
		assertEquals(2, v.length);

		v = v2.getVertices(1);
		assertArrayEquals(new double[]{0.8, 0.2}, v[0], 0.000001);
		assertArrayEquals(new double[]{0.6, 0.4}, v[1], 0.000001);
		assertArrayEquals(new double[]{0.6, 0.4}, v[2], 0.000001);
		assertEquals(3, v.length);

		v2 = vf.marginalize(1);
		v = v2.getVertices(0);
		assertArrayEquals(new double[]{0.8, 0.2}, v[0], 0.00000001);
		assertArrayEquals(new double[]{0.5, 0.5}, v[1], 0.00000001);
		assertEquals(2, v.length);

		v = v2.getVertices(1);
		assertArrayEquals(new double[]{0.2, 0.8}, v[0], 0.00000001);
		assertArrayEquals(new double[]{0.6, 0.4}, v[1], 0.00000001);
		assertArrayEquals(new double[]{0.6, 0.4}, v[2], 0.00000001);
		assertEquals(3, v.length);

		// marginalize all left side
		VertexFactor empty = vf.marginalize(1, 2);
		assertEquals(0, empty.getDataDomain().getSize());
		for (int s = 0; s < 2; ++s) {

			// we do not have here pruning so this is not simplified
			assertEquals(vf.getVertices(s).length, empty.getVertices(s).length);

			for (double[] vx : empty.getVertices(s)) {
				assertArrayEquals(new double[]{1}, vx, 0.00000001);
			}
		}
	}

	@Test
	public void testFilter() {
		VertexFactor vf = VertexFactorFactory.factory().domain(Strides.as(1, 4), Strides.as(0, 3, 5, 2))
				.addVertex(new double[]{0.1, 0.3, 0.2, 0.4}, 0, 1)
				.addVertex(new double[]{0.3, 0.2, 0.1, 0.4}, 0, 1)
				.addVertex(new double[]{0.1, 0.3, 0.4, 0.2}, 0, 1)
				.addVertex(new double[]{0.3, 0.3, 0.2, 0.2}, 1, 1)
				.addVertex(new double[]{0.6, 0.1, 0.2, 0.1}, 2, 1)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 2, 1)

				.addVertex(new double[]{0.7, 0.1, 0.1, 0.1}, 0, 0)
				.addVertex(new double[]{0.2, 0.3, 0.3, 0.2}, 0, 0)
				.addVertex(new double[]{0.1, 0.1, 0.7, 0.1}, 1, 0)
				.addVertex(new double[]{0.4, 0.2, 0.2, 0.2}, 1, 0)
				.addVertex(new double[]{0.3, 0.3, 0.3, 0.1}, 1, 0)
				.addVertex(new double[]{0.6, 0.1, 0.2, 0.1}, 2, 0)

				.build();

		VertexFactor v2 = vf.filter(5, 0).filter(0, 1);
		double[][] d = v2.getVertices();
		assertEquals(3, d.length);
		assertArrayEquals(new double[]{0.1, 0.1, 0.7, 0.1}, d[0], 1e-7);
		assertArrayEquals(new double[]{0.4, 0.2, 0.2, 0.2}, d[1], 1e-7);
		assertArrayEquals(new double[]{0.3, 0.3, 0.3, 0.1}, d[2], 1e-7);
	}

	@Test
	public void testCombine() {
		VertexFactor vf1_05 = VertexFactorFactory.factory().domain(Strides.as(1, 2), Strides.as(0, 2, 5, 2))
				.addVertex(new double[]{0.3, 0.7}, 0, 0)
				.addVertex(new double[]{0.4, 0.6}, 0, 1)
				.addVertex(new double[]{0.7, 0.3}, 0, 1)

				.addVertex(new double[]{0.9, 0.1}, 1, 0)
				.addVertex(new double[]{0.6, 0.4}, 1, 0)
				.addVertex(new double[]{0.5, 0.5}, 1, 1)
				.addVertex(new double[]{0.9, 0.1}, 1, 1)
				.build();

		VertexFactor vf5_0 = VertexFactorFactory.factory().domain(Strides.as(5, 2), Strides.as(0, 2))
				.addVertex(new double[]{0.5, 0.5}, 0)
				.addVertex(new double[]{0.6, 0.4}, 0)

				.addVertex(new double[]{0.2, 0.8}, 1)
				.addVertex(new double[]{0.3, 0.7}, 1)
				.build();

		VertexFactor v2_1 = vf1_05.combine(vf5_0);

		assertEquals(4, v2_1.getVertices(0).length);
		assertArrayEquals(new double[]{0.3 * 0.5, 0.7 * 0.5, 0.4 * 0.5, 0.6 * 0.5}, v2_1.getVertices(0)[0], 1e-8);
		assertArrayEquals(new double[]{0.3 * 0.5, 0.7 * 0.5, 0.7 * 0.5, 0.3 * 0.5}, v2_1.getVertices(0)[1], 1e-8);
		assertArrayEquals(new double[]{0.3 * 0.6, 0.7 * 0.6, 0.4 * 0.4, 0.6 * 0.4}, v2_1.getVertices(0)[2], 1e-10);
		assertArrayEquals(new double[]{0.3 * 0.6, 0.7 * 0.6, 0.7 * 0.4, 0.3 * 0.4}, v2_1.getVertices(0)[3], 1e-10);

		assertEquals(8, v2_1.getVertices(1).length);

		// first looping on the argument's vertices
		assertArrayEquals(new double[]{0.9 * 0.2, 0.1 * 0.2, 0.5 * 0.8, 0.5 * 0.8}, v2_1.getVertices(1)[0], 1e-8);
		assertArrayEquals(new double[]{0.6 * 0.2, 0.4 * 0.2, 0.5 * 0.8, 0.5 * 0.8}, v2_1.getVertices(1)[1], 1e-8);
		assertArrayEquals(new double[]{0.9 * 0.2, 0.1 * 0.2, 0.9 * 0.8, 0.1 * 0.8}, v2_1.getVertices(1)[2], 1e-8);
		assertArrayEquals(new double[]{0.6 * 0.2, 0.4 * 0.2, 0.9 * 0.8, 0.1 * 0.8}, v2_1.getVertices(1)[3], 1e-8);

		assertArrayEquals(new double[]{0.9 * 0.3, 0.1 * 0.3, 0.5 * 0.7, 0.5 * 0.7}, v2_1.getVertices(1)[4], 1e-8);
		assertArrayEquals(new double[]{0.6 * 0.3, 0.4 * 0.3, 0.5 * 0.7, 0.5 * 0.7}, v2_1.getVertices(1)[5], 1e-8);
		assertArrayEquals(new double[]{0.9 * 0.3, 0.1 * 0.3, 0.9 * 0.7, 0.1 * 0.7}, v2_1.getVertices(1)[6], 1e-8);
		assertArrayEquals(new double[]{0.6 * 0.3, 0.4 * 0.3, 0.9 * 0.7, 0.1 * 0.7}, v2_1.getVertices(1)[7], 1e-8);

		VertexFactor v2_2 = v2_1.marginalize(5);
		//	v2_2.convex();
		System.out.println(v2_2);

	}


	@Test
	public void testCombine2() {
		VertexFactor vf1_05 = VertexFactorFactory.factory().domain(Strides.var(1, 2), Strides.var(0, 2).and(5, 2))
				.addVertex(new double[]{0.3, 0.7}, 0, 0)
				.addVertex(new double[]{0.4, 0.6}, 0, 1)
				.addVertex(new double[]{0.7, 0.3}, 0, 1)

				.addVertex(new double[]{0.9, 0.1}, 1, 0)
				.addVertex(new double[]{0.6, 0.4}, 1, 0)
				.addVertex(new double[]{0.5, 0.5}, 1, 1)
				.addVertex(new double[]{0.9, 0.1}, 1, 1)
				.build();

		VertexFactor vf5 = VertexFactorFactory.factory().domain(Strides.as(5, 2), Strides.EMPTY)
				.addVertex(new double[]{0.5, 0.5})
				.addVertex(new double[]{0.6, 0.4})
				.build();

		VertexFactor v2_1 = vf1_05.combine(vf5);

		assertEquals(4, v2_1.getVertices(0).length);
		assertArrayEquals(new double[]{0.3 * 0.5, 0.7 * 0.5, 0.4 * 0.5, 0.6 * 0.5}, v2_1.getVertices(0)[0],
				1e-8);
		assertArrayEquals(new double[]{0.3 * 0.5, 0.7 * 0.5, 0.7 * 0.5, 0.3 * 0.5}, v2_1.getVertices(0)[1],
				1e-8);
		assertArrayEquals(new double[]{0.3 * 0.6, 0.7 * 0.6, 0.4 * 0.4, 0.6 * 0.4}, v2_1.getVertices(0)[2],
				1e-10);
		assertArrayEquals(new double[]{0.3 * 0.6, 0.7 * 0.6, 0.7 * 0.4, 0.3 * 0.4}, v2_1.getVertices(0)[3],
				1e-10);

		assertEquals(8, v2_1.getVertices(1).length);

		// first looping on the argument's vertices
//		assertArrayEquals(new double[] { 0.9 * 0.2, 0.1 * 0.2, 0.5 * 0.8, 0.5 * 0.8 }, v2_1.getVertices(1)[0], 0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.2, 0.4 * 0.2, 0.5 * 0.8, 0.5 * 0.8 }, v2_1.getVertices(1)[1], 0.00000001);
//		assertArrayEquals(new double[] { 0.9 * 0.2, 0.1 * 0.2, 0.9 * 0.8, 0.1 * 0.8 }, v2_1.getVertices(1)[2], 0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.2, 0.4 * 0.2, 0.9 * 0.8, 0.1 * 0.8 }, v2_1.getVertices(1)[3], 0.00000001);

//		assertArrayEquals(new double[] { 0.9 * 0.3, 0.1 * 0.3, 0.5 * 0.7, 0.5 * 0.7 }, v2_1.getVertices(1)[4], 0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.3, 0.4 * 0.3, 0.5 * 0.7, 0.5 * 0.7 }, v2_1.getVertices(1)[5], 0.00000001);
//		assertArrayEquals(new double[] { 0.9 * 0.3, 0.1 * 0.3, 0.9 * 0.7, 0.1 * 0.7 }, v2_1.getVertices(1)[6], 0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.3, 0.4 * 0.3, 0.9 * 0.7, 0.1 * 0.7 }, v2_1.getVertices(1)[7], 0.00000001);

		VertexFactor v2_2 = v2_1.marginalize(5);
		// v2_2.convex();
		System.out.println(v2_2);
	}
}
