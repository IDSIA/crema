package ch.idsia.crema.inference.jtree.algorithm.updating;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.model.graphical.Graph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
		BeliefPropagation<BayesianFactor, Graph> bp = new BeliefPropagation<>(model);
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

	@Test
	public void testPropagationSymbolic() {
		SparseModel<SymbolicFactor> m = new SparseModel<>();
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
}