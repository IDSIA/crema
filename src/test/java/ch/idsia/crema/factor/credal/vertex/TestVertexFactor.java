package ch.idsia.crema.factor.credal.vertex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;

public class TestVertexFactor {

	@Test
	public void testFilterLeft() {
		SparseModel<IntervalFactor> model = new SparseModel<>();
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		VertexFactor factorx = new VertexFactor(model.getDomain(A, B), model.getDomain(C));
		// A0B0 A1B0 A0B1 A1B1
		factorx.addVertex(new double[] { 0.3, 0.5, 0.1, 0.1 }, 1);
		factorx.addVertex(new double[] { 0.5, 0.2, 0.2, 0.1 }, 1);
		factorx.addVertex(new double[] { 0.1, 0.2, 0.4, 0.3 }, 0);
		factorx.addVertex(new double[] { 0.1, 0.3, 0.2, 0.2 }, 0);

		factorx = factorx.filter(A, 1).filter(B, 0);
		double[][] x = factorx.getVertices(0);
		assertEquals(2, x.length);
		assertArrayEquals(new double[] { 0.2 }, x[0], .0000001);
		assertArrayEquals(new double[] { 0.3 }, x[1], .0000001);

		x = factorx.getVertices(1);
		assertEquals(2, x.length);
		assertArrayEquals(new double[] { 0.5 }, x[0], .0000001);
		assertArrayEquals(new double[] { 0.2 }, x[1], .0000001);

	}

	@Test
	public void testLeftFilter2() {
		SparseModel<IntervalFactor> model = new SparseModel<>();
		int A = model.addVariable(2);
		int B = model.addVariable(3);
		int C = model.addVariable(2);

		VertexFactor factorx = new VertexFactor(model.getDomain(A, B, C), model.getDomain());

		factorx.addVertex(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });
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

		assertArrayEquals(new double[] { 3, 4, 9, 10 }, x1[0], 0.00000001);
		assertArrayEquals(new double[] { 5, 6, 11, 12 }, x2[0], 0.00000001);

		assertArrayEquals(new double[] { 7, 8, 9, 10, 11, 12 }, x3[0], 0.00000001);
		assertArrayEquals(new double[] { 2, 4, 6, 8, 10, 12 }, x4[0], 0.00000001);

	}

	@Test
	public void testAdd() {
		VertexFactor vf = new VertexFactor(Strides.as(1, 4), Strides.as(0, 3).and(5, 2));
		vf.addVertex(new double[] { 0.1, 0.3, 0.2, 0.4 }, 2, 1);
		assertArrayEquals(new double[] { 0.1, 0.3, 0.2, 0.4 }, vf.getVertices(2, 1)[0], 0.000001);
		assertNull(vf.getVertices(0, 1));
	}

	@Test
	public void testExpansion() {
		// p(X1|X0,X5)
		VertexFactor vf = new VertexFactor(Strides.as(1, 4), Strides.as(0, 3).and(5, 2));

		vf.addVertex(new double[] { 0.1, 0.3, 0.2, 0.4 }, 0, 1);
		vf.addVertex(new double[] { 0.3, 0.2, 0.1, 0.4 }, 0, 1);
		vf.addVertex(new double[] { 0.1, 0.3, 0.4, 0.2 }, 0, 1);
		vf.addVertex(new double[] { 0.3, 0.3, 0.2, 0.2 }, 1, 1);
		vf.addVertex(new double[] { 0.6, 0.1, 0.2, 0.1 }, 2, 1);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 2, 1);

		vf.addVertex(new double[] { 0.7, 0.1, 0.1, 0.1 }, 0, 0);
		vf.addVertex(new double[] { 0.2, 0.3, 0.3, 0.2 }, 0, 0);
		vf.addVertex(new double[] { 0.1, 0.1, 0.7, 0.1 }, 1, 0);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 1, 0);
		vf.addVertex(new double[] { 0.3, 0.3, 0.3, 0.1 }, 1, 0);
		vf.addVertex(new double[] { 0.6, 0.1, 0.2, 0.1 }, 2, 0);
		vf.addVertex(new double[] { 0.1, 0.2, 0.2, 0.5 }, 2, 0);

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
		assertArrayEquals(new double[] { 0.7, 0.1, 0.1, 0.1, 0.1, 0.3, 0.2, 0.4 }, dta[0][0], 0.00001);
		assertArrayEquals(new double[] { 0.2, 0.3, 0.3, 0.2, 0.1, 0.3, 0.2, 0.4 }, dta[0][1], 0.00001);
		assertArrayEquals(new double[] { 0.2, 0.3, 0.3, 0.2, 0.3, 0.2, 0.1, 0.4 }, dta[0][3], 0.00001);
		assertArrayEquals(new double[] { 0.4, 0.2, 0.2, 0.2, 0.3, 0.3, 0.2, 0.2 }, dta[1][1], 0.00001);
		assertArrayEquals(new double[] { 0.6, 0.1, 0.2, 0.1, 0.4, 0.2, 0.2, 0.2 }, dta[2][2], 0.00001);

		v2 = vf.reseparate(Strides.as(5, 2));
		dta = v2.getData();
		assertEquals(2, dta.length);

		// we removed var 0 from the separation domain
		assertEquals(12, dta[0].length);
		assertEquals(6, dta[1].length);
		assertEquals(12, dta[1][0].length);

		// test some vertices
		assertArrayEquals(new double[] { 0.7, 0.1, 0.6, 0.1, 0.1, 0.1, 0.1, 0.7, 0.2, 0.1, 0.1, 0.1 }, dta[0][0],
				0.00001);
		assertArrayEquals(new double[] { 0.7, 0.1, 0.6, 0.1, 0.1, 0.1, 0.1, 0.7, 0.2, 0.1, 0.1, 0.1 }, dta[0][0],
				0.00001);

		// no reseparation!

		VertexFactor vf1_05 = new VertexFactor(Strides.as(1, 2), Strides.as(0, 2, 5, 2));
		vf1_05.addVertex(new double[] { 0.3, 0.7 }, 0, 0);
		vf1_05.addVertex(new double[] { 0.4, 0.6 }, 0, 1);
		vf1_05.addVertex(new double[] { 0.7, 0.3 }, 0, 1);
		vf1_05.addVertex(new double[] { 0.9, 0.1 }, 1, 0);
		vf1_05.addVertex(new double[] { 0.6, 0.4 }, 1, 0);
		vf1_05.addVertex(new double[] { 0.5, 0.5 }, 1, 1);
		vf1_05.addVertex(new double[] { 0.9, 0.1 }, 1, 1);

		v2 = vf1_05.reseparate(vf1_05.getSeparatingDomain());
		int s = 0;

		double[][][] source = vf1_05.getData(), destination = v2.getData();
		assertEquals(source.length, destination.length);
		for (; s < source.length; ++s) {
			assertEquals(source[s].length, destination[s].length);
			for (int v = 0; v < source[s].length; ++v) {
				assertArrayEquals(source[s][v], destination[s][v], 0.0000001);
			}
		}

	}

	@Test
	public void testMarginalize() {
		VertexFactor vf = new VertexFactor(Strides.as(1, 2, 2, 2), Strides.as(0, 2));

		vf.addVertex(new double[] { 0.6, 0.2, 0.1, 0.1 }, 0);
		vf.addVertex(new double[] { 0.2, 0.3, 0.4, 0.1 }, 0);
		vf.addVertex(new double[] { 0.1, 0.1, 0.7, 0.1 }, 1);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 1);
		vf.addVertex(new double[] { 0.3, 0.3, 0.3, 0.1 }, 1);

		VertexFactor v2 = vf.marginalize(2);
		double[][] v = v2.getVertices(0);
		assertArrayEquals(new double[] { 0.7, 0.3 }, v[0], 0.000001);
		assertArrayEquals(new double[] { 0.6, 0.4 }, v[1], 0.000001);
		assertEquals(2, v.length);

		v = v2.getVertices(1);
		assertArrayEquals(new double[] { 0.8, 0.2 }, v[0], 0.000001);
		assertArrayEquals(new double[] { 0.6, 0.4 }, v[1], 0.000001);
		assertArrayEquals(new double[] { 0.6, 0.4 }, v[2], 0.000001);
		assertEquals(3, v.length);

		v2 = vf.marginalize(1);
		v = v2.getVertices(0);
		assertArrayEquals(new double[] { 0.8, 0.2 }, v[0], 0.00000001);
		assertArrayEquals(new double[] { 0.5, 0.5 }, v[1], 0.00000001);
		assertEquals(2, v.length);

		v = v2.getVertices(1);
		assertArrayEquals(new double[] { 0.2, 0.8 }, v[0], 0.00000001);
		assertArrayEquals(new double[] { 0.6, 0.4 }, v[1], 0.00000001);
		assertArrayEquals(new double[] { 0.6, 0.4 }, v[2], 0.00000001);
		assertEquals(3, v.length);

		// marginalize all left side
		VertexFactor empty = vf.marginalize(1, 2);
		assertEquals(0, empty.getDataDomain().getSize());
		for (int s = 0; s < 2; ++s) {

			// we do not have here pruning so this is not simplified
			assertEquals(vf.getVertices(s).length, empty.getVertices(s).length);

			for (double[] vx : empty.getVertices(s)) {
				assertArrayEquals(new double[] { 1 }, vx, 0.00000001);
			}
		}
	}

	@Test
	public void testFilter() {
		VertexFactor vf = new VertexFactor(Strides.as(1, 4), Strides.as(0, 3, 5, 2));
		vf.addVertex(new double[] { 0.1, 0.3, 0.2, 0.4 }, 0, 1);
		vf.addVertex(new double[] { 0.3, 0.2, 0.1, 0.4 }, 0, 1);
		vf.addVertex(new double[] { 0.1, 0.3, 0.4, 0.2 }, 0, 1);
		vf.addVertex(new double[] { 0.3, 0.3, 0.2, 0.2 }, 1, 1);
		vf.addVertex(new double[] { 0.6, 0.1, 0.2, 0.1 }, 2, 1);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 2, 1);

		vf.addVertex(new double[] { 0.7, 0.1, 0.1, 0.1 }, 0, 0);
		vf.addVertex(new double[] { 0.2, 0.3, 0.3, 0.2 }, 0, 0);
		vf.addVertex(new double[] { 0.1, 0.1, 0.7, 0.1 }, 1, 0);
		vf.addVertex(new double[] { 0.4, 0.2, 0.2, 0.2 }, 1, 0);
		vf.addVertex(new double[] { 0.3, 0.3, 0.3, 0.1 }, 1, 0);
		vf.addVertex(new double[] { 0.6, 0.1, 0.2, 0.1 }, 2, 0);

		VertexFactor v2 = vf.filter(5, 0).filter(0, 1);
		double[][] d = v2.getVertices();
		assertEquals(3, d.length);
		assertArrayEquals(new double[] { 0.1, 0.1, 0.7, 0.1 }, d[0], 0.0000001);
		assertArrayEquals(new double[] { 0.4, 0.2, 0.2, 0.2 }, d[1], 0.0000001);
		assertArrayEquals(new double[] { 0.3, 0.3, 0.3, 0.1 }, d[2], 0.0000001);
	}

	@Test
	public void testCombine() {
		VertexFactor vf1_05 = new VertexFactor(Strides.as(1, 2), Strides.as(0, 2, 5, 2));
		vf1_05.addVertex(new double[] { 0.3, 0.7 }, 0, 0);
		vf1_05.addVertex(new double[] { 0.4, 0.6 }, 0, 1);
		vf1_05.addVertex(new double[] { 0.7, 0.3 }, 0, 1);

		vf1_05.addVertex(new double[] { 0.9, 0.1 }, 1, 0);
		vf1_05.addVertex(new double[] { 0.6, 0.4 }, 1, 0);
		vf1_05.addVertex(new double[] { 0.5, 0.5 }, 1, 1);
		vf1_05.addVertex(new double[] { 0.9, 0.1 }, 1, 1);

		VertexFactor vf5_0 = new VertexFactor(Strides.as(5, 2), Strides.as(0, 2));
		vf5_0.addVertex(new double[] { 0.5, 0.5 }, 0);
		vf5_0.addVertex(new double[] { 0.6, 0.4 }, 0);

		vf5_0.addVertex(new double[] { 0.2, 0.8 }, 1);
		vf5_0.addVertex(new double[] { 0.3, 0.7 }, 1);

		VertexFactor v2_1 = vf1_05.combine(vf5_0);

		assertEquals(4, v2_1.getVertices(0).length);
		assertArrayEquals(new double[] { 0.3 * 0.5, 0.7 * 0.5, 0.4 * 0.5, 0.6 * 0.5 }, v2_1.getVertices(0)[0],
				0.00000001);
		assertArrayEquals(new double[] { 0.3 * 0.5, 0.7 * 0.5, 0.7 * 0.5, 0.3 * 0.5 }, v2_1.getVertices(0)[1],
				0.00000001);
		assertArrayEquals(new double[] { 0.3 * 0.6, 0.7 * 0.6, 0.4 * 0.4, 0.6 * 0.4 }, v2_1.getVertices(0)[2],
				0.0000000001);
		assertArrayEquals(new double[] { 0.3 * 0.6, 0.7 * 0.6, 0.7 * 0.4, 0.3 * 0.4 }, v2_1.getVertices(0)[3],
				0.0000000001);

		assertEquals(8, v2_1.getVertices(1).length);

		// first looping on the argument's vertices
		assertArrayEquals(new double[] { 0.9 * 0.2, 0.1 * 0.2, 0.5 * 0.8, 0.5 * 0.8 }, v2_1.getVertices(1)[0],
				0.00000001);
		assertArrayEquals(new double[] { 0.6 * 0.2, 0.4 * 0.2, 0.5 * 0.8, 0.5 * 0.8 }, v2_1.getVertices(1)[1],
				0.00000001);
		assertArrayEquals(new double[] { 0.9 * 0.2, 0.1 * 0.2, 0.9 * 0.8, 0.1 * 0.8 }, v2_1.getVertices(1)[2],
				0.00000001);
		assertArrayEquals(new double[] { 0.6 * 0.2, 0.4 * 0.2, 0.9 * 0.8, 0.1 * 0.8 }, v2_1.getVertices(1)[3],
				0.00000001);

		assertArrayEquals(new double[] { 0.9 * 0.3, 0.1 * 0.3, 0.5 * 0.7, 0.5 * 0.7 }, v2_1.getVertices(1)[4],
				0.00000001);
		assertArrayEquals(new double[] { 0.6 * 0.3, 0.4 * 0.3, 0.5 * 0.7, 0.5 * 0.7 }, v2_1.getVertices(1)[5],
				0.00000001);
		assertArrayEquals(new double[] { 0.9 * 0.3, 0.1 * 0.3, 0.9 * 0.7, 0.1 * 0.7 }, v2_1.getVertices(1)[6],
				0.00000001);
		assertArrayEquals(new double[] { 0.6 * 0.3, 0.4 * 0.3, 0.9 * 0.7, 0.1 * 0.7 }, v2_1.getVertices(1)[7],
				0.00000001);

		VertexFactor v2_2 = v2_1.marginalize(5);
	//	v2_2.convex();
		System.out.println(v2_2);

	}

	
	
	
	@Test
	public void testCombine2() {
		VertexFactor vf1_05 = new VertexFactor(Strides.var(1, 2), Strides.var(0, 2).and(5, 2));
		vf1_05.addVertex(new double[] { 0.3, 0.7 }, 0, 0);
		vf1_05.addVertex(new double[] { 0.4, 0.6 }, 0, 1);
		vf1_05.addVertex(new double[] { 0.7, 0.3 }, 0, 1);

		vf1_05.addVertex(new double[] { 0.9, 0.1 }, 1, 0);
		vf1_05.addVertex(new double[] { 0.6, 0.4 }, 1, 0);
		vf1_05.addVertex(new double[] { 0.5, 0.5 }, 1, 1);
		vf1_05.addVertex(new double[] { 0.9, 0.1 }, 1, 1);

		VertexFactor vf5 = new VertexFactor(Strides.as(5, 2), Strides.EMPTY);
		vf5.addVertex(new double[] { 0.5, 0.5 });
		vf5.addVertex(new double[] { 0.6, 0.4 });

		VertexFactor v2_1 = vf1_05.combine(vf5);

		assertEquals(4, v2_1.getVertices(0).length);
		assertArrayEquals(new double[] { 0.3 * 0.5, 0.7 * 0.5, 0.4 * 0.5, 0.6 * 0.5 }, v2_1.getVertices(0)[0],
				0.00000001);
		assertArrayEquals(new double[] { 0.3 * 0.5, 0.7 * 0.5, 0.7 * 0.5, 0.3 * 0.5 }, v2_1.getVertices(0)[1],
				0.00000001);
		assertArrayEquals(new double[] { 0.3 * 0.6, 0.7 * 0.6, 0.4 * 0.4, 0.6 * 0.4 }, v2_1.getVertices(0)[2],
				0.0000000001);
		assertArrayEquals(new double[] { 0.3 * 0.6, 0.7 * 0.6, 0.7 * 0.4, 0.3 * 0.4 }, v2_1.getVertices(0)[3],
				0.0000000001);

		assertEquals(8, v2_1.getVertices(1).length);
//
//		// first looping on the argument's vertices
//		assertArrayEquals(new double[] { 0.9 * 0.2, 0.1 * 0.2, 0.5 * 0.8, 0.5 * 0.8 }, v2_1.getVertices(1)[0],
//				0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.2, 0.4 * 0.2, 0.5 * 0.8, 0.5 * 0.8 }, v2_1.getVertices(1)[1],
//				0.00000001);
//		assertArrayEquals(new double[] { 0.9 * 0.2, 0.1 * 0.2, 0.9 * 0.8, 0.1 * 0.8 }, v2_1.getVertices(1)[2],
//				0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.2, 0.4 * 0.2, 0.9 * 0.8, 0.1 * 0.8 }, v2_1.getVertices(1)[3],
//				0.00000001);
//
//		assertArrayEquals(new double[] { 0.9 * 0.3, 0.1 * 0.3, 0.5 * 0.7, 0.5 * 0.7 }, v2_1.getVertices(1)[4],
//				0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.3, 0.4 * 0.3, 0.5 * 0.7, 0.5 * 0.7 }, v2_1.getVertices(1)[5],
//				0.00000001);
//		assertArrayEquals(new double[] { 0.9 * 0.3, 0.1 * 0.3, 0.9 * 0.7, 0.1 * 0.7 }, v2_1.getVertices(1)[6],
//				0.00000001);
//		assertArrayEquals(new double[] { 0.6 * 0.3, 0.4 * 0.3, 0.9 * 0.7, 0.1 * 0.7 }, v2_1.getVertices(1)[7],
//				0.00000001);

		VertexFactor v2_2 = v2_1.marginalize(5);
		//v2_2.convex();
		System.out.println(v2_2);

	}
}
