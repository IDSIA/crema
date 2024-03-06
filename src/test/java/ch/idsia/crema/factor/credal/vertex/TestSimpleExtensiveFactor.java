package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.ExtensiveVertexDefaultAlgebra;
import ch.idsia.crema.factor.algebra.OnlineConvexHullAlgebra;
import ch.idsia.crema.factor.algebra.Operation;
import ch.idsia.crema.factor.algebra.vertex.VertexHashStrategy;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSimpleExtensiveFactor {

	@Test
	public void x() {

		DAGModel<IntervalFactor> model = new DAGModel<>();
		int x0 = model.addVariable(2);
		int x1 = model.addVariable(2);

		VertexFactor factorx = VertexFactorFactory.factory().domain(model.getDomain(x0, x1), Strides.EMPTY)
				.addVertex(new double[]{0.1, 0.2, 0.4, 0.3})
				.addVertex(new double[]{0.3, 0.5, 0.1, 0.1})
				.addVertex(new double[]{0.1, 0.3, 0.2, 0.2})
				.addVertex(new double[]{0.5, 0.2, 0.2, 0.1})
				.get();

		factorx = factorx.filter(x1, 1).filter(x0, 0);

		Strides domain1 = model.getDomain(x0);
		VertexFactor factor1 = VertexFactorFactory.factory().domain(domain1, Strides.EMPTY)
				.addVertex(new double[]{0.1, 0.9})
				.addVertex(new double[]{0.2, 0.8})
				.get();

		VertexFactor factor2 = VertexFactorFactory.factory().domain(model.getDomain(x1), model.getDomain(x0))
				.addVertex(new double[]{0.1, 0.2, 0.9, 0.8})
				.addVertex(new double[]{0.3, 0.5, 0.7, 0.5})
				.addVertex(new double[]{0.1, 0.5, 0.9, 0.5})
				.addVertex(new double[]{0.3, 0.2, 0.7, 0.8})
				.get();

		// convertiamo:
		IntervalFactor f1h = IntervalFactorFactory.factory().domain(domain1, Strides.EMPTY)
				.lower(new double[]{0.1, 0.2})
				.upper(new double[]{0.8, 0.9})
				.get();

		IntervalFactor f2h = IntervalFactorFactory.factory().domain(model.getDomain(x1), model.getDomain(x0))
				.lower(new double[]{0.1, 0.2}, 0)
				.upper(new double[]{0.8, 0.9}, 0)
				.lower(new double[]{0.4, 0.3}, 1)
				.upper(new double[]{0.7, 0.6}, 1)
				.get();

		model.setFactor(x0, f1h);
		model.setFactor(x1, f2h);
	}

	@Test
	public void defaultAlgebra() {
		// factor (v0 | v2)
		Strides domain = DomainBuilder.var(0, 2).size(3, 3).strides();
		ExtensiveVertexFactor factor = ExtensiveVertexFactorFactory.factory().domain(domain)
				// populate with some data
				.addVertex(new double[]{0.1, 0.2, 0.7, 0.3, 0.3, 0.4, 0.4, 0.5, 0.1})
				.addVertex(new double[]{0.2, 0.6, 0.2, 0.4, 0.2, 0.4, 0.1, 0.1, 0.8})
				.addVertex(new double[]{0.3, 0.6, 0.1, 0.6, 0.2, 0.2, 0.4, 0.1, 0.5})
				.get();

		Strides domain2 = DomainBuilder.var(2).size(3).strides();
		ExtensiveVertexFactor factor2 = ExtensiveVertexFactorFactory.factory().domain(domain2)
				.addVertex(new double[]{0.1, 0.1, 0.8})
				.addVertex(new double[]{0.3, 0.4, 0.3})
				.addVertex(new double[]{0.5, 0.3, 0.2})
				.addVertex(new double[]{0.7, 0.2, 0.1})
				.get();

		ExtensiveVertexDefaultAlgebra algebra = new ExtensiveVertexDefaultAlgebra();

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

		ObjectOpenCustomHashSet<double[]> seen = new ObjectOpenCustomHashSet<>(new VertexHashStrategy());
		seen.add(a);
		System.out.println(seen.contains(a));
		System.out.println(seen.contains(b));
		System.out.println(seen.contains(c));
	}

	@Test
	public void avoidDuplicateVerteces() {

		// factor (v0 | v2)
		Strides domain = DomainBuilder.var(0, 2).size(3, 3).strides();
		ExtensiveVertexFactor factor = ExtensiveVertexFactorFactory.factory().domain(domain)

				// populate with some data
				.addVertex(new double[]{0.1, 0.2, 0.7, 0.3, 0.3, 0.4, 0.4, 0.5, 0.1})
				.addVertex(new double[]{0.2, 0.6, 0.2, 0.4, 0.2, 0.4, 0.1, 0.1, 0.8})
				.addVertex(new double[]{0.3, 0.6, 0.1, 0.6, 0.2, 0.2, 0.4, 0.1, 0.5})
				.get();

		Strides domain2 = DomainBuilder.var(2).size(3).strides();
		ExtensiveVertexFactor factor2 = ExtensiveVertexFactorFactory.factory().domain(domain2)
				.addVertex(new double[]{0.1, 0.1, 0.8})
				.addVertex(new double[]{0.3, 0.4, 0.3})
				.addVertex(new double[]{0.5, 0.3, 0.2})
				.addVertex(new double[]{0.7, 0.2, 0.1})
				.get();

		Operation<ExtensiveVertexFactor> algebra = new OnlineConvexHullAlgebra() {
			final ObjectOpenCustomHashSet<double[]> seen = new ObjectOpenCustomHashSet<>(new VertexHashStrategy());

			@Override
			protected boolean canAddVector(List<double[]> vertex, double[] vector) {
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
