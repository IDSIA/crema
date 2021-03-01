package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.03.2021 18:42
 */
public class LoopyBeliefPropagationTest {

	@Test
	public void testPropagationQuery() throws Exception {
		// source: Jensen, p.110, Fig. 4.1 "A simple Bayesian network BN".
		BayesianNetwork model = new BayesianNetwork();
		int A0 = model.addVariable(2);
		int A1 = model.addVariable(2);
		int A2 = model.addVariable(2);
		int A3 = model.addVariable(2);
		int A4 = model.addVariable(2);
		int A5 = model.addVariable(2);

		model.addParent(A1, A0);
		model.addParent(A2, A0);
		model.addParent(A3, A1);
		model.addParent(A4, A1);
		model.addParent(A4, A2);
		model.addParent(A5, A2);

		BayesianFactor[] factors = new BayesianFactor[model.getVariables().length];

		factors[A0] = new BayesianFactor(model.getDomain(A0), new double[]{.7, .3});
		factors[A1] = new BayesianFactor(model.getDomain(A0, A1), new double[]{.4, .3, .6, .7});
		factors[A2] = new BayesianFactor(model.getDomain(A0, A2), new double[]{.5, .8, .5, .2});
		factors[A3] = new BayesianFactor(model.getDomain(A1, A3), new double[]{.6, .1, .4, .9});
		factors[A4] = new BayesianFactor(model.getDomain(A1, A2, A4), new double[]{.1, .8, .4, .7, .9, .2, .6, .3});
		factors[A5] = new BayesianFactor(model.getDomain(A2, A5), new double[]{.4, .5, .6, .5});

		model.setFactors(factors);

		LoopyBeliefPropagation<BayesianFactor> lbp = new LoopyBeliefPropagation<>(model);
		lbp.setIterations(1);

		assertEquals(lbp.messages.size(), lbp.neighbours.size());

		for (int i : model.getVariables()) {
			for (int j : model.getVariables()) {
				if (i == j) continue;

				final DefaultEdge edge = model.getNetwork().getEdge(i, j);
				if (edge == null) continue;

				var key = new ImmutablePair<>(i, j);

				assertTrue(lbp.messages.containsKey(key));
				assertTrue(lbp.neighbours.containsKey(key));
			}
		}

		BayesianFactor factor = lbp.query(A0);

		assertEquals(factors[A0], factor);
	}
}