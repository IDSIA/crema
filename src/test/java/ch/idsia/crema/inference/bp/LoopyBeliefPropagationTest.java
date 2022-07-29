package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.io.bif.BIFParser;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

import java.util.List;

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

		factors[A0] = BayesianFactorFactory.factory().domain(model.getDomain(A0)).data(new double[]{.7, .3}).get();
		factors[A1] = BayesianFactorFactory.factory().domain(model.getDomain(A0, A1)).data(new double[]{.4, .3, .6, .7}).get();
		factors[A2] = BayesianFactorFactory.factory().domain(model.getDomain(A0, A2)).data(new double[]{.5, .8, .5, .2}).get();
		factors[A3] = BayesianFactorFactory.factory().domain(model.getDomain(A1, A3)).data(new double[]{.6, .1, .4, .9}).get();
		factors[A4] = BayesianFactorFactory.factory().domain(model.getDomain(A1, A2, A4)).data(new double[]{.1, .8, .4, .7, .9, .2, .6, .3}).get();
		factors[A5] = BayesianFactorFactory.factory().domain(model.getDomain(A2, A5)).data(new double[]{.4, .5, .6, .5}).get();

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

		factors[A] = BayesianFactorFactory.factory().domain(model.getDomain(A)).data(new double[]{.4, .6}).get();
		factors[B] = BayesianFactorFactory.factory().domain(model.getDomain(A, B)).data(new double[]{.3, .9, .7, .1}).get();
		factors[C] = BayesianFactorFactory.factory().domain(model.getDomain(A, C)).data(new double[]{.2, .7, .8, .3}).get();

		model.setFactors(factors);

		LoopyBeliefPropagation<BayesianFactor> lbp = new LoopyBeliefPropagation<>();

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
		factors[A] = BayesianFactorFactory.factory().domain(bn.getDomain(A)).data(new double[]{.2, .8}).get();
		factors[B] = BayesianFactorFactory.factory().domain(bn.getDomain(A, B)).data(new double[]{.2, .6, .8, .4}).get();
		factors[C] = BayesianFactorFactory.factory().domain(bn.getDomain(B, C)).data(new double[]{.3, .2, .7, .8}).get();
		factors[D] = BayesianFactorFactory.factory().domain(bn.getDomain(C, D)).data(new double[]{.9, .6, .1, .4}).get();

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

	@Test
	void testVariableElimination() {
		final BayesianNetwork model = BayesianNetworkContainer.mix5Variables().network;

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{4, 3, 1, 0, 2});
		final LoopyBeliefPropagation<BayesianFactor> lbp = new LoopyBeliefPropagation<>();

		TIntIntMap evidence;
		BayesianFactor Qlbp;
		BayesianFactor Qve;

		evidence = new TIntIntHashMap();
		Qlbp = lbp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LBP: P(Rain) =                                     " + Qlbp);
		System.out.println("VE:  P(Rain) =                                     " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap();
		evidence.put(3, 0);
		evidence.put(4, 1);
		Qlbp = lbp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LBP: P(Rain|Wet Grass = false, Slippery = true) =  " + Qlbp);
		System.out.println("VE:  P(Rain|Wet Grass = false, Slippery = true) =  " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.05);

		evidence = new TIntIntHashMap();
		evidence.put(3, 0);
		evidence.put(4, 0);
		Qlbp = lbp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LBP: P(Rain|Wet Grass = false, Slippery = false) = " + Qlbp);
		System.out.println("VE:  P(Rain|Wet Grass = false, Slippery = false) = " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap();
		evidence.put(0, 1);
		Qlbp = lbp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LBP: P(Rain|Winter = true) =                       " + Qlbp);
		System.out.println("VE:  P(Rain|Winter = true) =                       " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap();
		evidence.put(0, 0);
		Qlbp = lbp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LBP: P(Rain|Winter = false) =                      " + Qlbp);
		System.out.println("VE:  P(Rain|Winter = false) =                      " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);
	}

	@Test
	void testObsOnQueryVariable() {
		final BayesianNetwork model = BayesianNetworkContainer.mix5Variables().network;

		final BeliefPropagation<BayesianFactor> lbp = new BeliefPropagation<>();

		final TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(0, 1);

		final BayesianFactor q = lbp.query(model, evidence, 0);
		System.out.println(q);

		assertEquals(1.0, q.getValue(0));
	}

	@Test
	void testDisjointGraph() {
		final DAGModel<BayesianFactor> m = new DAGModel<>();

		final int s1 = m.addVariable(2);
		final int s2 = m.addVariable(2);
		final int q1 = m.addVariable(2);

		m.addParent(q1, s1);

		final BayesianFactor f1 = BayesianFactorFactory.factory()
				.domain(m.getDomain(s1))
				.set(0.5, 0)
				.set(0.5, 1)
				.get();
		final BayesianFactor f2 = BayesianFactorFactory.factory()
				.domain(m.getDomain(s2))
				.set(0.5, 0)
				.set(0.5, 1)
				.get();
		final BayesianFactor f3 = BayesianFactorFactory.factory()
				.domain(m.getDomain(q1, s1))
				.set(0.2, 0, 0)
				.set(0.8, 1, 0)
				.set(0.6, 0, 1)
				.set(0.4, 1, 1)
				.get();

		m.setFactor(s1, f1);
		m.setFactor(s2, f2);
		m.setFactor(q1, f3);

		final LoopyBeliefPropagation<BayesianFactor> inf = new LoopyBeliefPropagation<>();

		final TIntIntMap obs = new TIntIntHashMap();
		obs.put(q1, 1);

		final List<BayesianFactor> queries = inf.query(m, obs, s1, s2);

		System.out.println(queries);

		// assert that no exception has been raised
	}
}