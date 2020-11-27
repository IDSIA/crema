package ch.idsia.crema.factor.credal.vertex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.algebra.DefaultExtensiveAlgebra;
import ch.idsia.crema.factor.credal.vertex.algebra.OnlineConvexHullAlgebra;
import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.math.Operation;
import ch.idsia.crema.model.vertex.VertexHashStrategy;
import gnu.trove.set.hash.TCustomHashSet;

public class TestSimpleExtensiveFactor {

	@Test
	public void x() {

		DAGModel<IntervalFactor> model = new DAGModel<>();
		int x0 = model.addVariable(2);
		int x1 = model.addVariable(2);


		VertexFactor factorx = new VertexFactor(model.getDomain(x0, x1), Strides.EMPTY);
		factorx.addVertex(new double[]{0.1, 0.2, 0.4, 0.3});
		factorx.addVertex(new double[]{0.3, 0.5, 0.1, 0.1});
		factorx.addVertex(new double[]{0.1, 0.3, 0.2, 0.2});
		factorx.addVertex(new double[]{0.5, 0.2, 0.2, 0.1});

		factorx = factorx.filter(x1, 1).filter(x0, 0);

		Strides domain1 = model.getDomain(x0);
		VertexFactor factor1 = new VertexFactor(domain1, Strides.EMPTY);
		factor1.addVertex(new double[]{0.1, 0.9});
		factor1.addVertex(new double[]{0.2, 0.8});

		VertexFactor factor2 = new VertexFactor(model.getDomain(x1), model.getDomain(x0));
		factor2.addVertex(new double[]{0.1, 0.2, 0.9, 0.8});
		factor2.addVertex(new double[]{0.3, 0.5, 0.7, 0.5});
		factor2.addVertex(new double[]{0.1, 0.5, 0.9, 0.5});
		factor2.addVertex(new double[]{0.3, 0.2, 0.7, 0.8});

		// convertiamo:
		IntervalFactor f1h = new IntervalFactor(domain1, Strides.EMPTY);
		f1h.setLower(new double[]{0.1, 0.2});
		f1h.setUpper(new double[]{0.8, 0.9});

		IntervalFactor f2h = new IntervalFactor(model.getDomain(x1), model.getDomain(x0));
		f2h.setLower(new double[]{0.1, 0.2}, 0);
		f2h.setUpper(new double[]{0.8, 0.9}, 0);
		f2h.setLower(new double[]{0.4, 0.3}, 1);
		f2h.setUpper(new double[]{0.7, 0.6}, 1);

		model.setFactor(x0, f1h);
		model.setFactor(x1, f2h);

	}

	@Test
	public void defaultAlgebra() {

		// factor (v0 | v2)
		Strides domain = DomainBuilder.var(0, 2).size(3, 3).strides();
		ExtensiveVertexFactor factor = new ExtensiveVertexFactor(domain, false);

		// populate with some data
		factor.addVertex(new double[]{0.1, 0.2, 0.7, 0.3, 0.3, 0.4, 0.4, 0.5, 0.1});
		factor.addVertex(new double[]{0.2, 0.6, 0.2, 0.4, 0.2, 0.4, 0.1, 0.1, 0.8});
		factor.addVertex(new double[]{0.3, 0.6, 0.1, 0.6, 0.2, 0.2, 0.4, 0.1, 0.5});

		Strides domain2 = DomainBuilder.var(2).size(3).strides();
		ExtensiveVertexFactor factor2 = new ExtensiveVertexFactor(domain2, false);
		factor2.addVertex(new double[]{0.1, 0.1, 0.8});
		factor2.addVertex(new double[]{0.3, 0.4, 0.3});
		factor2.addVertex(new double[]{0.5, 0.3, 0.2});
		factor2.addVertex(new double[]{0.7, 0.2, 0.1});

		DefaultExtensiveAlgebra algebra = new DefaultExtensiveAlgebra();

		ExtensiveVertexFactor f3 = algebra.combine(factor, factor2);

		// since we have no convex hull yet we will end up with many vertices
		// to be exact 3 * 4
		assertEquals(12, f3.getInternalVertices().size());

		ExtensiveVertexFactor f4 = algebra.marginalize(f3, 2);
		assertEquals(12, f4.getInternalVertices().size());

		// marginalize everything and we have to end up with [1]
		ExtensiveVertexFactor f5 = algebra.marginalize(f4, 0);

		assertEquals(12, f5.getInternalVertices().size());
		for (double[] data : f5.getInternalVertices()) {
			assertArrayEquals(new double[]{1}, data, 0.0000000001);
		}
	}

	@Test
	public void testHash() {
		double[] a = new double[]{0.1, 0.1, 0.8};
		double[] b = new double[]{0.1, 0.1, 0.8};
		double[] c = new double[]{0.10001, 0.1, 0.79999};

		TCustomHashSet<double[]> seen = new TCustomHashSet<>(new VertexHashStrategy());
		seen.add(a);
		System.out.println(seen.contains(a));
		System.out.println(seen.contains(b));
		System.out.println(seen.contains(c));
	}

	@Test
	public void avoidDuplicateVerteces() {

		// factor (v0 | v2)
		Strides domain = DomainBuilder.var(0, 2).size(3, 3).strides();
		ExtensiveVertexFactor factor = new ExtensiveVertexFactor(domain, false);

		// populate with some data
		factor.addVertex(new double[]{0.1, 0.2, 0.7, 0.3, 0.3, 0.4, 0.4, 0.5, 0.1});
		factor.addVertex(new double[]{0.2, 0.6, 0.2, 0.4, 0.2, 0.4, 0.1, 0.1, 0.8});
		factor.addVertex(new double[]{0.3, 0.6, 0.1, 0.6, 0.2, 0.2, 0.4, 0.1, 0.5});

		Strides domain2 = DomainBuilder.var(2).size(3).strides();
		ExtensiveVertexFactor factor2 = new ExtensiveVertexFactor(domain2, false);
		factor2.addVertex(new double[]{0.1, 0.1, 0.8});
		factor2.addVertex(new double[]{0.3, 0.4, 0.3});
		factor2.addVertex(new double[]{0.5, 0.3, 0.2});
		factor2.addVertex(new double[]{0.7, 0.2, 0.1});

		Operation<ExtensiveVertexFactor> algebra = new OnlineConvexHullAlgebra() {

			TCustomHashSet<double[]> seen = new TCustomHashSet<>(new VertexHashStrategy());

			@Override
			protected boolean canAddVector(ExtensiveVertexFactor factor, double[] vector) {
				if (seen.contains(vector)) {
					return false;
				} else {
					seen.add(vector);
					return true;
				}
			}
		};

		ExtensiveVertexFactor f3 = algebra.combine(factor, factor2);

		// since we have no convex hull yet we will end up with many vertices
		// to be exact 3 * 4
		assertEquals(12, f3.getInternalVertices().size());

		ExtensiveVertexFactor f4 = algebra.marginalize(f3, 2);
		// assertEquals(12, f4.getInternalVertices().size());

		// marginalize everything and we have to end up with [1]
		ExtensiveVertexFactor f5 = algebra.marginalize(f4, 0);

		assertEquals(1, f5.getInternalVertices().size());
		for (double[] data : f5.getInternalVertices()) {
			assertArrayEquals(new double[]{1}, data, 0.0000000001);
		}
	}
}
