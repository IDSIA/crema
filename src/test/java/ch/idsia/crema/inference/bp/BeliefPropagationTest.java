package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.io.bif.BIFParser;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:52
 */
public class BeliefPropagationTest {

	// TODO: add better tests...

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

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>();
		BayesianFactor factor = bp.query(model, A0);

		assertEquals(factors[A0], factor);

		bp.distributingEvidence(A0);
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

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>();

		// P(A):
		TIntIntHashMap obs = new TIntIntHashMap();
		BayesianFactor q = bp.query(model, obs, A);
		System.out.println("P(A):              " + q);
		assertArrayEquals(new double[]{.4, .6}, q.getData(), 1e-6);

		// P(A | B=0)
		obs.put(B, 0);
		q = bp.query(model, obs, A);
		System.out.println("P(A | B=0):       " + q);
		assertArrayEquals(new double[]{.1818, .8182}, q.getData(), 1e-3);

		// P(A | B=1)
		obs.put(B, 1);
		q = bp.query(model, obs, A);
		System.out.println("P(A | B=1):       " + q);
		assertArrayEquals(new double[]{.8235, .1765}, q.getData(), 1e-3);

		// P(A | B=0, C=0)
		obs.put(B, 0);
		obs.put(C, 0);
		q = bp.query(model, obs, A);
		System.out.println("P(A | B=0, C=0): " + q);
		assertArrayEquals(new double[]{.0597, .9403}, q.getData(), 1e-3);

		// P(A | B=1, C=1)
		obs.put(B, 1);
		obs.put(C, 1);
		q = bp.query(model, obs, A);
		System.out.println("P(A | B=1, C=1): " + q);
		assertArrayEquals(new double[]{.9256, .0744}, q.getData(), 1e-3);
	}

	@Ignore // TODO: this need method filter() implemented for SymbolicFactors
	@Test
	public void testPropagationSymbolic() {
		DAGModel<SymbolicFactor> m = new DAGModel<>();
		int A = m.addVariable(2);
		int B = m.addVariable(2);
		int C = m.addVariable(2);

		m.addParent(A, C);
		m.addParent(B, C);

		BayesianFactor fAC = new BayesianFactor(m.getDomain(A));
		BayesianFactor fBC = new BayesianFactor(m.getDomain(B));
		BayesianFactor fC = new BayesianFactor(m.getDomain(C));

		PriorFactor pAC = new PriorFactor(fAC);
		PriorFactor pBC = new PriorFactor(fBC);
		PriorFactor pC = new PriorFactor(fC);

		m.setFactor(A, pAC);
		m.setFactor(B, pBC);
		m.setFactor(C, pC);

		BeliefPropagation<SymbolicFactor> bp = new BeliefPropagation<>();
		bp.setModel(m);
		SymbolicFactor factor = bp.fullPropagation();

		System.out.println(factor);
	}

	@Test
	public void testInference() {
		// TODO: this is similar to testCollectingEvidenceWithObs(): merge the two or remove one
		BayesianNetwork bn = new BayesianNetwork();
		int A = bn.addVariable(2);
		int B = bn.addVariable(2);
		int C = bn.addVariable(2);

		bn.addParent(B, A);
		bn.addParent(C, A);

		BayesianFactor[] factors = new BayesianFactor[bn.getVariables().length];
		factors[A] = new BayesianFactor(bn.getDomain(A), new double[]{.5, .5});
		factors[B] = new BayesianFactor(bn.getDomain(A, B), new double[]{.8, .2, .2, .8});
		factors[C] = new BayesianFactor(bn.getDomain(A, C), new double[]{.4, .6, .6, .4});

		bn.setFactors(factors);

		assertArrayEquals(new int[]{B, C}, bn.getChildren(A));
		assertArrayEquals(new int[]{A}, bn.getParents(C));
		assertArrayEquals(new int[]{A}, bn.getParents(B));

		TIntIntMap obs = new TIntIntHashMap();
		BayesianFactor q;

		BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>();
		inf.setModel(bn);

		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.5, .5}, q.getData(), 1e-6);

		obs.put(C, 0);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.4, .6}, q.getData(), 1e-6);

		obs.put(C, 1);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.6, .4}, q.getData(), 1e-6);

		obs = new TIntIntHashMap();

		obs.put(B, 0);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.8, .2}, q.getData(), 1e-6);

		obs.put(B, 1);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.2, .8}, q.getData(), 1e-6);

		obs.put(C, 1);
		obs.put(B, 1);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.2727, .7273}, q.getData(), 1e-3);

		obs.put(C, 0);
		obs.put(B, 1);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.1429, .8571}, q.getData(), 1e-3);

		obs.put(C, 1);
		obs.put(B, 0);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.8571, .1429}, q.getData(), 1e-3);

		obs.put(C, 0);
		obs.put(B, 0);
		q = inf.query(bn, obs, A);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.7272, .2728}, q.getData(), 1e-3);
	}

	@Test
	public void bayesianNetworkFromExercise41() {
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
		BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>();
		inf.setModel(bn);

		TIntIntMap obs = new TIntIntHashMap();
		obs.put(D, 0);
		final BayesianFactor q = inf.query(bn, obs, A);
		System.out.println("query=" + q);

		assertEquals(res, q);
	}

	@Test
	public void testAlloyNumberOfFactorsPerClique() throws IOException {
		final BayesianNetwork network = BIFParser.read("models/bif/alloy.bif").network;

		final BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>();
		bp.setModel(network);

		final long factors = bp.potentialsPerClique.values().stream()
				.mapToLong(Set::size)
				.sum();

		assertEquals(bp.potentialsPerClique.size(), bp.getJunctionTree().vertexSet().size());

		for (Clique clique : bp.getJunctionTree().vertexSet()) {
			assertTrue(clique + " not found!", bp.potentialsPerClique.containsKey(clique));
		}

		assertEquals(network.getVariables().length, factors);

		bp.potentialsPerClique.forEach((c, f) -> {
			assertNotNull(f);
			assertNotNull(c);
			assertTrue(f.size() > 0);
		});
	}

}