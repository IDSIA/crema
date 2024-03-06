package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.IndexIterator;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.08.2021 10:56
 */
class BayesianOrFactorTest {

	@Test
	void testingModelWithOrNode() {
		final DAGModel<BayesianFactor> m = new DAGModel<>();
		final int A = m.addVariable(2);
		final int B = m.addVariable(2);
		final int O = m.addVariable(2);
		m.addParents(O, A, B);

		final BayesianFactor fA = new BayesianDefaultFactor(m.getDomain(A), new double[]{.3, .7});
		final BayesianFactor fB = new BayesianDefaultFactor(m.getDomain(B), new double[]{.6, .4});
		final BayesianFactor OR = new BayesianOrFactor(m.getDomain(A, B, O), A, B);

		m.setFactor(A, fA);
		m.setFactor(B, fB);
		m.setFactor(O, OR);

		System.out.printf("vars=%s%n", Arrays.toString(OR.getDomain().getVariables()));

		final double[] expected = {1., 0., 0., 0., 0., 1., 1., 1.};

		final IndexIterator it = OR.getDomain().getIterator();
		int i = 0;
		while (it.hasNext()) {
			final int offset = it.next();
			final double v = OR.getValueAt(offset);
			final int[] states = OR.getDomain().getStatesFor(offset);

			System.out.printf("states=%10s | %3d | %.1f%n", Arrays.toString(states), i, v);
			Assertions.assertEquals(expected[i], v);
			i++;
		}

		final BayesianDefaultFactor one = BayesianFactorFactory.one(O);
		final BayesianDefaultFactor zero = BayesianFactorFactory.zero(O);

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{A, B, O});

		final BayesianFactor q_a1 = ve.query(m, new Int2IntOpenHashMap(new int[]{A}, new int[]{1}), O);
		final BayesianFactor q_b1 = ve.query(m, new Int2IntOpenHashMap(new int[]{B}, new int[]{1}), O);
		final BayesianFactor q_ab0 = ve.query(m, new Int2IntOpenHashMap(new int[]{A, B}, new int[]{0, 0}), O);

		Assertions.assertEquals(one, q_a1);
		Assertions.assertEquals(one, q_b1);
		Assertions.assertEquals(zero, q_ab0);
	}

	@Test
	void testFilterToOne() {
		final BayesianFactor o = new BayesianOrFactor(DomainBuilder.var(1, 2).size(2, 2).strides(), 1);
		final BayesianFactor b = o.filter(1, 1);
		final BayesianFactor one = BayesianFactorFactory.one(2);

		Assertions.assertEquals(one, b);
	}

	@Test
	void testFilterToZero() {
		final BayesianFactor o = new BayesianOrFactor(DomainBuilder.var(1, 2).size(2, 2).strides(), 1);
		final BayesianFactor a = o.filter(1, 0);
		final BayesianFactor zero = BayesianFactorFactory.zero(2);

		Assertions.assertEquals(zero, a);
	}

	@Test
	void testFilterMultipleParents() {
		final BayesianFactor o = new BayesianOrFactor(DomainBuilder.var(1, 2, 3).size(2, 2, 2).strides(), 1, 2);
		final BayesianFactor a = o.filter(1, 0);
		final BayesianFactor b = o.filter(2, 0);
		final BayesianFactor c = o.filter(1, 1);
		final BayesianFactor d = o.filter(2, 1);
		final BayesianFactor e = o.filter(1, 0).filter(2, 0);

		final BayesianFactor aa = a.filter(2, 1);
		final BayesianFactor bb = b.filter(1, 1);

		final BayesianFactor one = BayesianFactorFactory.one(3);
		final BayesianFactor zero = BayesianFactorFactory.zero(3);

		Assertions.assertEquals(one, aa);
		Assertions.assertEquals(one, bb);
		Assertions.assertEquals(one, c);
		Assertions.assertEquals(one, d);
		Assertions.assertEquals(zero, e);

		Assertions.assertTrue(a instanceof BayesianOrFactor);
		Assertions.assertTrue(b instanceof BayesianOrFactor);
	}
}