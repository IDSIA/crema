package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.io.bif.BIFParser;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.03.2021 18:42
 */
public class LoopyBeliefPropagationTest {

	@Test
	public void testPropagationQuery() {
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

		LoopyBeliefPropagation<BayesianFactor> lbp = new LoopyBeliefPropagation<>();
		lbp.setIterations(1);

		/*
		TODO: we are using pre-processing so these checks are not valid anymore
		for (int i : model.getVariables()) {
			for (int j : model.getVariables()) {
				if (i == j) continue;

				final DefaultEdge edge = model.getNetwork().getEdge(i, j);
				if (edge == null) continue;

				var key = new ImmutablePair<>(i, j);

				assertTrue(lbp.neighbours.containsKey(key));
			}
		}

		assertEquals(lbp.messages.size(), lbp.neighbours.size());
		 */

		BayesianFactor factor = lbp.query(model, A0);
		assertEquals(factors[A0], factor);
	}

	@Test
	public void testCollectingEvidenceWithObs() {
		BayesianNetwork model = new BayesianNetwork();
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		model.addParent(B, A);
		model.addParent(C, A);

		BayesianFactor[] factors = new BayesianFactor[model.getVariables().length];

		factors[A] = new BayesianFactor(model.getDomain(A), new double[]{.4, .6});
		factors[B] = new BayesianFactor(model.getDomain(A, B), new double[]{.3, .9, .7, .1});
		factors[C] = new BayesianFactor(model.getDomain(A, C), new double[]{.2, .7, .8, .3});

		model.setFactors(factors);

		LoopyBeliefPropagation<BayesianFactor> lbp = new LoopyBeliefPropagation<>();
		lbp.setModel(model);

		// P(A):
		TIntIntHashMap obs = new TIntIntHashMap();
		BayesianFactor q = lbp.query(model, obs, A);
		System.out.println("P(A):              " + q);
		assertArrayEquals(new double[]{.4, .6}, q.getData(), 1e-6);

		// P(A | B=0)
		obs.put(B, 0);
		q = lbp.query(model, obs, A);
		System.out.println("P(A | B=0):       " + q);
		assertArrayEquals(new double[]{.1818, .8182}, q.getData(), 1e-3);

		// P(A | B=1)
		obs.put(B, 1);
		q = lbp.query(model, obs, A);
		System.out.println("P(A | B=1):       " + q);
		assertArrayEquals(new double[]{.8235, .1765}, q.getData(), 1e-3);

		// P(A | B=0, C=0)
		obs.put(B, 0);
		obs.put(C, 0);
		q = lbp.query(model, obs, A);
		System.out.println("P(A | B=0, C=0): " + q);
		assertArrayEquals(new double[]{.0597, .9403}, q.getData(), 1e-3);

		// P(A | B=1, C=1)
		obs.put(B, 1);
		obs.put(C, 1);
		q = lbp.query(model, obs, A);
		System.out.println("P(A | B=1, C=1): " + q);
		assertArrayEquals(new double[]{.9256, .0744}, q.getData(), 1e-3);
	}

	@Test
	public void testBayesianNetworkFromExercise41() {
		BayesianNetwork bn = new BayesianNetwork();
		int A = bn.addVariable(2);
		int B = bn.addVariable(2);
		int C = bn.addVariable(2);
		int D = bn.addVariable(2);

		bn.addParent(B, A);
		bn.addParent(C, B);
		bn.addParent(D, C);

		BayesianFactor[] factors = new BayesianFactor[bn.getVariables().length];
		factors[A] = new BayesianFactor(bn.getDomain(A), new double[]{.2, .8});
		factors[B] = new BayesianFactor(bn.getDomain(A, B), new double[]{.2, .6, .8, .4});
		factors[C] = new BayesianFactor(bn.getDomain(B, C), new double[]{.3, .2, .7, .8});
		factors[D] = new BayesianFactor(bn.getDomain(C, D), new double[]{.9, .6, .1, .4});

		bn.setFactors(factors);

		// computations by hand
		final BayesianFactor phi1 = factors[D].filter(D, 0);   // Sum_D P(D|C) * e_d
		final BayesianFactor phi2 = factors[C].combine(phi1);       // P(C|B) * phi1
		final BayesianFactor phi3 = phi2.marginalize(C);            // Sum_C phi2
		final BayesianFactor phi4 = factors[B].combine(phi3);       // P(B|A) * phi3
		final BayesianFactor phi5 = phi4.marginalize(B);            // Sum_B phi4
		final BayesianFactor phi6 = factors[A].combine(phi5);       // P(A) * phi5
		final BayesianFactor res1 = phi6.normalize();

		// computations by hand using messages
		final BayesianFactor psi1 = factors[D].filter(D, 0);
		final BayesianFactor psi2 = factors[B].combine(factors[C]).combine(psi1).marginalize(C);
		final BayesianFactor phiS = factors[A].combine(psi2).marginalize(B);
		final BayesianFactor res = phiS.normalize();

		assertEquals(res1, res);

		// computation using Belief Propagation
		LoopyBeliefPropagation<BayesianFactor> inf = new LoopyBeliefPropagation<>();

		TIntIntMap obs = new TIntIntHashMap();
		obs.put(D, 0);
		final BayesianFactor q = inf.query(bn, obs, A);
		System.out.println("query=" + q);

		assertEquals(res, q);
	}

	@Test
	public void testNumberOfStatesReturned() throws Exception {
		final BayesianNetwork network = BIFParser.read("models/bif/alloy.bif").network;
		final LoopyBeliefPropagation<BayesianFactor> lbp = new LoopyBeliefPropagation<>();

//		int[] vs = {4, 5, 25};

		for (int v : network.getVariables()) {
			final BayesianFactor q0 = lbp.query(network, v);
//			System.out.println(v + ":\t" + q0.getData().length + "\t" + network.getSize(v));
//			System.out.println(q0);

			assertEquals(network.getSize(v), q0.getData().length);
		}
	}
}