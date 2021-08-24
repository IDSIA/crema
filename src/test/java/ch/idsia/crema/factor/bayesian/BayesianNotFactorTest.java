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
class BayesianNotFactorTest {

	@Test
	void testingModelWithNotNode() {
		final DAGModel<BayesianFactor> m = new DAGModel<>();
		final int A = m.addVariable(2);
		final int N = m.addVariable(2);
		m.addParents(N, A);

		final BayesianFactor fA = new BayesianDefaultFactor(m.getDomain(A), new double[]{.3, .7});
		final BayesianFactor OR = new BayesianOrFactor(m.getDomain(A, N), A);

		m.setFactor(A, fA);
		m.setFactor(N, OR);

		System.out.printf("vars=%s%n", Arrays.toString(OR.getDomain().getVariables()));

		final double[] expected = {1., 0., 0., 1.};

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

		final BayesianDefaultFactor one = BayesianFactorFactory.one(N);
		final BayesianDefaultFactor zero = BayesianFactorFactory.zero(N);

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{A, N});

		final BayesianFactor q_a1 = ve.query(m, new TIntIntHashMap(new int[]{A}, new int[]{1}), N);
		final BayesianFactor q_a0 = ve.query(m, new TIntIntHashMap(new int[]{A}, new int[]{0}), N);

		Assertions.assertEquals(zero, q_a0);
		Assertions.assertEquals(one, q_a1);
	}

	@Test
	void testFilterToOne() {
		final BayesianFactor o = new BayesianNotFactor(DomainBuilder.var(1, 2).size(2, 2).strides(), 1);
		final BayesianFactor b = o.filter(1, 0);
		final BayesianFactor one = BayesianFactorFactory.one(2);

		Assertions.assertEquals(one, b);
	}

	@Test
	void testFilterToZero() {
		final BayesianFactor o = new BayesianNotFactor(DomainBuilder.var(1, 2).size(2, 2).strides(), 1);
		final BayesianFactor a = o.filter(1, 1);
		final BayesianFactor zero = BayesianFactorFactory.zero(2);

		Assertions.assertEquals(zero, a);
	}

	@Test
	void testFilterMultipleParents() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new BayesianNotFactor(DomainBuilder.var(1, 2, 3).size(2, 2, 2).strides(), 1, 2));
	}
}