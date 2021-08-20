package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.IndexIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
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

		final BayesianFactor TRUE = new BayesianDefaultFactor(m.getDomain(A), new double[]{0, 1});
		final BayesianFactor FALSE = new BayesianDefaultFactor(m.getDomain(B), new double[]{0, 1});
		final BayesianFactor OR = new BayesianOrFactor(m.getDomain(A, B, O), new int[]{A, B}, new int[]{1, 1});

		m.setFactor(A, TRUE);
		m.setFactor(B, FALSE);
		m.setFactor(O, OR);

		System.out.printf("vars=%s%n", Arrays.toString(OR.getDomain().getVariables()));

		final IndexIterator it = OR.getDomain().getIterator();
		int i = 0;
		while (it.hasNext()) {
			final int offset = it.next();
			final double v = OR.getValueAt(offset);
			final int[] states = OR.getDomain().getStatesFor(offset);

			System.out.printf("states=%10s | %3d | %.1f%n", Arrays.toString(states), i, v);
			i++;
		}

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{A, B, O});
		final TIntIntMap obs = new TIntIntHashMap();
		obs.put(A, 1);
		obs.put(B, 1);
		final BayesianFactor q = ve.query(m, obs, O);
		System.out.println(q);
	}
}