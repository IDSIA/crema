package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.IndexIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.08.2021 10:56
 */
class BayesianNoisyOrFactorTest {

	@Test
	void testingModelWithOrNode() {
		final DAGModel<BayesianFactor> m = new DAGModel<>();
		final int A = m.addVariable(2);
		final int B = m.addVariable(2);
		final int O = m.addVariable(2);
		m.addParents(O, A, B);

		final BayesianFactor TRUE = new BayesianDefaultFactor(m.getDomain(A), new double[]{.3, .7});
		final BayesianFactor FALSE = new BayesianDefaultFactor(m.getDomain(B), new double[]{.6, .4});
		final BayesianFactor OR = new BayesianNoisyOrFactor(m.getDomain(A, B, O), new int[]{A, B}, new double[]{.1, .2});

		m.setFactor(A, TRUE);
		m.setFactor(B, FALSE);
		m.setFactor(O, OR);

		System.out.printf("vars=%s%n", Arrays.toString(OR.getDomain().getVariables()));

		final double[] expected = {1., .1, .2, .02, .0, .9, .8, .98};

		final IndexIterator it = OR.getDomain().getIterator();
		int i = 0;
		while (it.hasNext()) {
			final int offset = it.next();
			final double v = OR.getValueAt(offset);
			final int[] states = OR.getDomain().getStatesFor(offset);

			System.out.printf("states=%10s | %3d | %.4f%n", Arrays.toString(states), i, v);
			Assertions.assertEquals(expected[i], v, 1e-3);
			i++;
		}

		final BayesianDefaultFactor one = BayesianFactorFactory.one(O);
		final BayesianDefaultFactor zero = BayesianFactorFactory.zero(O);

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{A, B, O});

		final BayesianFactor q_a1 = ve.query(m, new TIntIntHashMap(new int[]{A}, new int[]{1}), O);
		final BayesianFactor q_b1 = ve.query(m, new TIntIntHashMap(new int[]{B}, new int[]{1}), O);
		final BayesianFactor q_ab0 = ve.query(m, new TIntIntHashMap(new int[]{A, B}, new int[]{0, 0}), O);

		// TODO: chek results
		System.out.println(q_a1);
		System.out.println(q_b1);
		Assertions.assertEquals(zero, q_ab0);
	}

	@Test
	void testFilterToOne() {
		final BayesianFactor o = new BayesianNoisyOrFactor(DomainBuilder.var(1, 2).size(2, 2).strides(), new int[]{1}, new double[]{.1});
		final BayesianFactor b = o.filter(1, 1);
		final BayesianFactor one = BayesianFactorFactory.one(2);

		Assertions.assertEquals(one, b);
	}

	@Test
	void testFilterToZero() {
		final BayesianFactor o = new BayesianNoisyOrFactor(DomainBuilder.var(1, 2).size(2, 2).strides(), new int[]{1}, new double[]{.3});
		final BayesianFactor a = o.filter(1, 0);
		final BayesianFactor zero = BayesianFactorFactory.zero(2);

		Assertions.assertEquals(zero, a);
	}

	@Test
	void testFilterMultipleParents() {
		final BayesianFactor o = new BayesianNoisyOrFactor(DomainBuilder.var(1, 2, 3).size(2, 2, 2).strides(), new int[]{1, 2}, new double[]{.3, .4});
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

		Assertions.assertTrue(a instanceof BayesianNoisyOrFactor);
		Assertions.assertTrue(b instanceof BayesianNoisyOrFactor);
	}
}