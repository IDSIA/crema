package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
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
	BayesianNetwork model;
	BayesianFactor[] factors;

	// TODO: add better tests...

	int A0, A1, A2, A3, A4, A5;

	@Before
	public void setUp() {
		// source: Jensen, p.110, Fig. 4.1 "A simple Bayesian network BN".
		model = new BayesianNetwork();
		A0 = model.addVariable(2);
		A1 = model.addVariable(2);
		A2 = model.addVariable(2);
		A3 = model.addVariable(2);
		A4 = model.addVariable(2);
		A5 = model.addVariable(2);

		model.addParent(A1, A0);
		model.addParent(A2, A0);
		model.addParent(A3, A1);
		model.addParent(A4, A1);
		model.addParent(A4, A2);
		model.addParent(A5, A2);

		factors = new BayesianFactor[6];

		factors[A0] = new BayesianFactor(model.getDomain(A0));
		factors[A1] = new BayesianFactor(model.getDomain(A0, A1));
		factors[A2] = new BayesianFactor(model.getDomain(A0, A2));
		factors[A3] = new BayesianFactor(model.getDomain(A1, A3));
		factors[A4] = new BayesianFactor(model.getDomain(A1, A2, A4));
		factors[A5] = new BayesianFactor(model.getDomain(A5, A2));

		factors[A0].setData(new int[]{A0}, new double[]{.7, .3});
		factors[A1].setData(new int[]{A1, A0}, new double[]{.4, .6, .3, .7});
		factors[A2].setData(new int[]{A2, A0}, new double[]{.5, .5, .8, .2});
		factors[A3].setData(new int[]{A3, A1}, new double[]{.6, .4, .1, .9});
		factors[A4].setData(new int[]{A4, A1, A2}, new double[]{.1, .9, .8, .2, .4, .6, .6, .4});
		factors[A5].setData(new int[]{A2, A5}, new double[]{.4, .6, .5, .5});

		model.setFactors(factors);
	}

	@Test
	public void testPropagationQuery() {
		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(model);
		BayesianFactor factor = bp.fullPropagation();

		assertEquals(factor, factors[A0]);

		bp.distributingEvidence(A0);
	}

	@Test
	public void testCollectingEvidenceWithObs() {
		model = new BayesianNetwork();
		A0 = model.addVariable(2);
		A1 = model.addVariable(2);
		A2 = model.addVariable(2);

		model.addParent(A1, A0);
		model.addParent(A2, A0);

		factors = new BayesianFactor[3];

		factors[A0] = new BayesianFactor(model.getDomain(A0));
		factors[A1] = new BayesianFactor(model.getDomain(A0, A1));
		factors[A2] = new BayesianFactor(model.getDomain(A0, A2));

		factors[A0].setData(new int[]{A0}, new double[]{.4, .6});
		factors[A1].setData(new int[]{A1, A0}, new double[]{.3, .7, .7, .3});
		factors[A2].setData(new int[]{A2, A0}, new double[]{.2, .8, .8, .2});

		model.setFactors(factors);

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(model);

		// P(A0):
		bp.clearEvidence();
		System.out.println("P(A0):              " + bp.collectingEvidence(A0));

		// P(A0 | A1=0)
		TIntIntHashMap obs = new TIntIntHashMap();
		obs.put(A1, 0);
		bp.setEvidence(obs);
		System.out.println("P(A0 | A1=0):       " + bp.collectingEvidence(A0));

		// P(A0 | A1=1)
		obs = new TIntIntHashMap();
		obs.put(A1, 1);
		bp.setEvidence(obs);
		System.out.println("P(A0 | A1=1):       " + bp.collectingEvidence(A0));

		// P(A0 | A1=0, A2=0)
		obs = new TIntIntHashMap();
		obs.put(A1, 0);
		obs.put(A2, 0);
		bp.setEvidence(obs);
		System.out.println("P(A0 | A1=0, A2=0): " + bp.collectingEvidence(A0));

		// P(A0 | A1=1, A2=1)
		obs = new TIntIntHashMap();
		obs.put(A1, 1);
		obs.put(A2, 1);
		bp.setEvidence(obs);
		System.out.println("P(A0 | A1=1, A2=1): " + bp.collectingEvidence(A0));
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
		BayesianNetwork bn = new BayesianNetwork();
		int A = bn.addVariable(2); // skill:    A               (low, high)
		int H = bn.addVariable(2); // question: high interest   (a, b)
		int L = bn.addVariable(2); // question: low interest    (a, b)

		bn.addParent(H, A);
		bn.addParent(L, A);

		BayesianFactor[] factors = new BayesianFactor[3];
		factors[A] = new BayesianFactor(bn.getDomain(A));
		factors[H] = new BayesianFactor(bn.getDomain(A, H));
		factors[L] = new BayesianFactor(bn.getDomain(A, L));

		factors[A].setData(new double[]{.5, .5});
		factors[H].setData(new double[]{.9, .1, .1, .9});
		factors[L].setData(new double[]{.2, .8, .8, .2});

		bn.setFactors(factors);

		BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>(bn);


		assertArrayEquals(new int[]{H, L}, bn.getChildren(A));
		assertArrayEquals(new int[]{A}, bn.getParents(L));
		assertArrayEquals(new int[]{A}, bn.getParents(H));

		TIntIntMap obs = new TIntIntHashMap();

		obs.put(L, 0);
		inf.setEvidence(obs);
		BayesianFactor q = inf.query(A);

		assertArrayEquals(new double[]{.2, .8}, q.getData(), 1e-3);

		obs.put(L, 1);
		inf.setEvidence(obs);
		q = inf.query(A);
		assertArrayEquals(new double[]{.8, .2}, q.getData(), 1e-3);

		obs = new TIntIntHashMap();

		obs.put(H, 0);
		inf.setEvidence(obs);
		q = inf.query(A);
		assertArrayEquals(new double[]{.9, .1}, q.getData(), 1e-3);

		obs.put(H, 1);
		inf.setEvidence(obs);
		q = inf.query(A);
		assertArrayEquals(new double[]{.1, .9}, q.getData(), 1e-3);

		obs.put(L, 1);
		obs.put(H, 1);
		q = inf.query(A);
		assertArrayEquals(new double[]{.3077, .6923}, q.getData(), 1e-3);

		obs.put(L, 0);
		obs.put(H, 1);
		q = inf.query(A);
		assertArrayEquals(new double[]{.0270, .9730}, q.getData(), 1e-3);

		obs.put(L, 1);
		obs.put(H, 0);
		q = inf.query(A);
		assertArrayEquals(new double[]{.9730, .0270}, q.getData(), 1e-3);

		obs.put(L, 0);
		obs.put(H, 0);
		q = inf.query(A);
		assertArrayEquals(new double[]{0.6923, 0.3077}, q.getData(), 1e-3);
	}
}