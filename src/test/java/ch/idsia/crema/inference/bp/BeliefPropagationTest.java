package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
		factors[A1] = new BayesianFactor(model.getDomain(A0, A1), new double[]{.4, .6, .3, .7});
		factors[A2] = new BayesianFactor(model.getDomain(A0, A2), new double[]{.5, .5, .8, .2});
		factors[A3] = new BayesianFactor(model.getDomain(A1, A3), new double[]{.6, .4, .1, .9});
		factors[A4] = new BayesianFactor(model.getDomain(A1, A2, A4), new double[]{.1, .9, .4, .6, .8, .2, .7, .3});
		factors[A5] = new BayesianFactor(model.getDomain(A5, A2), new double[]{.4, .6, .5, .5});

		model.setFactors(factors);

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(model);
		BayesianFactor factor = bp.query(A0);

		assertEquals(factors[A0], factor);

		bp.distributingEvidence(A0);
	}

	@Test
	public void testCollectingEvidenceWithObs() {
		BayesianNetwork model = new BayesianNetwork();
		int A0 = model.addVariable(2);
		int A1 = model.addVariable(2);
		int A2 = model.addVariable(2);

		model.addParent(A1, A0);
		model.addParent(A2, A0);

		BayesianFactor[] factors = new BayesianFactor[model.getVariables().length];

		factors[A0] = new BayesianFactor(model.getDomain(A0), new double[]{.4, .6});
		factors[A1] = new BayesianFactor(model.getDomain(A0, A1), new double[]{.3, .9, .7, .1});
		factors[A2] = new BayesianFactor(model.getDomain(A0, A2), new double[]{.2, .75, .8, .25});

		model.setFactors(factors);

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(model);

		// P(A0):
		TIntIntHashMap obs = new TIntIntHashMap();
		BayesianFactor q = bp.query(A0, obs);
		System.out.println("P(A0):              " + q);
		assertArrayEquals(new double[]{.4, .6}, q.getData(), 1e-6);

		// P(A0 | A1=0)
		obs.put(A1, 0);
		q = bp.query(A0, obs);
		System.out.println("P(A0 | A1=0):       " + q);
		// TODO: there and below are missing asserts with correct computations

		// P(A0 | A1=1)
		obs.put(A1, 1);
		q = bp.query(A0, obs);
		System.out.println("P(A0 | A1=1):       " + q);

		// P(A0 | A1=0, A2=0)
		obs.put(A1, 0);
		obs.put(A2, 0);
		q = bp.query(A0, obs);
		System.out.println("P(A0 | A1=0, A2=0): " + q);

		// P(A0 | A1=1, A2=1)
		obs.put(A1, 1);
		obs.put(A2, 1);
		q = bp.query(A0, obs);
		System.out.println("P(A0 | A1=1, A2=1): " + q);
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

		BeliefPropagation<SymbolicFactor> bp = new BeliefPropagation<>(m);
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

		final BayesianFactor fA = factors[A];
		final BayesianFactor fB = factors[B];
		final BayesianFactor fC = factors[C];

		final BayesianFactor res = fA.combine(fB).marginalize(B).combine(fC).filter(C, 0).normalize();

		System.out.println(res);

		BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>(bn);

		assertArrayEquals(new int[]{B, C}, bn.getChildren(A));
		assertArrayEquals(new int[]{A}, bn.getParents(C));
		assertArrayEquals(new int[]{A}, bn.getParents(B));

		TIntIntMap obs = new TIntIntHashMap();
		BayesianFactor q;

		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		System.out.println(inf.query(A, obs));
		System.out.println(inf.query(B, obs));
		System.out.println(inf.query(C, obs));
		assertArrayEquals(new double[]{.5, .5}, q.getData(), .0);


		obs.put(C, 0);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.2, .8}, q.getData(), .0);

		obs.put(C, 1);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.8, .2}, q.getData(), .0);

		obs = new TIntIntHashMap();

		obs.put(B, 0);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.9, .1}, q.getData(), .0);

		obs.put(B, 1);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.1, .9}, q.getData(), .0);

		obs.put(C, 1);
		obs.put(B, 1);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		// TODO: check if these arrays are correct or not, same below
		assertArrayEquals(new double[]{.3077, .6923}, q.getData(), 1e-3);

		obs.put(C, 0);
		obs.put(B, 1);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.0270, .9730}, q.getData(), 1e-3);

		obs.put(C, 1);
		obs.put(B, 0);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{.9730, .0270}, q.getData(), 1e-3);

		obs.put(C, 0);
		obs.put(B, 0);
		q = inf.query(A, obs);
		System.out.println(q + " " + obs);
		assertArrayEquals(new double[]{0.6923, 0.3077}, q.getData(), 1e-3);
	}

	@Test
	public void bayesianNetworkFromExercise42() {
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
		final BayesianFactor phi1 = factors[D].filter(D, 0).marginalize(D);
		final BayesianFactor phi2 = factors[C].combine(phi1);
		final BayesianFactor phi3 = phi2.marginalize(C);
		final BayesianFactor phi4 = factors[B].combine(phi3);
		final BayesianFactor phi5 = phi4.marginalize(B);
		final BayesianFactor phi6 = factors[A].combine(phi5);
		final BayesianFactor res = phi6.normalize();

		System.out.println("Φ1=" + phi1);
		System.out.println("Φ2=" + phi2);
		System.out.println("Φ3=" + phi3);
		System.out.println("Φ4=" + phi4);
		System.out.println("Φ5=" + phi5);
		System.out.println("Φ6=" + phi6);
		System.out.println("res=" + res);

		// computation using Belief Propagation
		BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>(bn);

		TIntIntMap obs = new TIntIntHashMap();
		obs.put(D, 0);
		final BayesianFactor q = inf.query(A, obs);
		System.out.println("query=" + q);

		assertEquals(res, q);
	}

}