package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.algebra.FactorAlgebra;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.factor.symbolic.serialize.SymbolicExecution;
import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.io.bif.BIFParser;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

		factors[A0] = BayesianFactorFactory.factory().domain(model.getDomain(A0)).data(new double[]{.7, .3}).get();
		factors[A1] = BayesianFactorFactory.factory().domain(model.getDomain(A0, A1)).data(new double[]{.4, .3, .6, .7}).get();
		factors[A2] = BayesianFactorFactory.factory().domain(model.getDomain(A0, A2)).data(new double[]{.5, .8, .5, .2}).get();
		factors[A3] = BayesianFactorFactory.factory().domain(model.getDomain(A1, A3)).data(new double[]{.6, .1, .4, .9}).get();
		factors[A4] = BayesianFactorFactory.factory().domain(model.getDomain(A1, A2, A4)).data(new double[]{.1, .8, .4, .7, .9, .2, .6, .3}).get();
		factors[A5] = BayesianFactorFactory.factory().domain(model.getDomain(A2, A5)).data(new double[]{.4, .5, .6, .5}).get();

		model.setFactors(factors);

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(false);
		BayesianFactor factor = bp.fullPropagation(model, A0);

		assertEquals(factors[A0], factor);
		assertTrue(bp.isFullyPropagated());

		System.out.println(bp.queryFullPropagated(A1));
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

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>();

		// P(A):
		Int2IntMap obs = new Int2IntOpenHashMap();
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

	@Test
	public void testPropagationSymbolic() {
		final DAGModel<SymbolicFactor> m = new DAGModel<>();
		final int A = m.addVariable(2);
		final int B = m.addVariable(2);
		final int C = m.addVariable(2);

		m.addParent(B, A);
		m.addParent(C, A);

		BayesianFactor fAC = BayesianFactorFactory.factory().domain(m.getDomain(A))
				.data(new double[]{.4, .6}).get();
		BayesianFactor fBC = BayesianFactorFactory.factory().domain(m.getDomain(A, B))
				.data(new double[]{.1, .9, .2, .8}).get();
		BayesianFactor fC = BayesianFactorFactory.factory().domain(m.getDomain(A, C))
				.data(new double[]{.3, .7, .5, .5})
				.get();

		PriorFactor pAC = new PriorFactor(fAC);
		PriorFactor pBC = new PriorFactor(fBC);
		PriorFactor pC = new PriorFactor(fC);

		m.setFactor(A, pAC);
		m.setFactor(B, pBC);
		m.setFactor(C, pC);

		BeliefPropagation<SymbolicFactor> bp = new BeliefPropagation<>(false);
		SymbolicFactor factor = bp.query(m, A);

		System.out.println(fAC.getDomain());
		System.out.println(fBC.getDomain());
		System.out.println(fC.getDomain());
		System.out.println(factor);

		SymbolicExecution<BayesianFactor> se = new SymbolicExecution<>(new FactorAlgebra<BayesianFactor>());
		final BayesianFactor res = se.exec(factor);

		System.out.println(res);
	}

	@Test
	public void testInference() {
		// TODO: this is similar to testCollectingEvidenceWithObs(): merge the two or remove one
		final BayesianNetwork bn = new BayesianNetwork();
		final int A = bn.addVariable(2);
		final int B = bn.addVariable(2);
		final int C = bn.addVariable(2);

		bn.addParent(B, A);
		bn.addParent(C, A);

		final BayesianFactor[] factors = new BayesianFactor[bn.getVariables().length];
		factors[A] = BayesianFactorFactory.factory().domain(bn.getDomain(A)).data(new double[]{.5, .5}).get();
		factors[B] = BayesianFactorFactory.factory().domain(bn.getDomain(A, B)).data(new double[]{.8, .2, .2, .8}).get();
		factors[C] = BayesianFactorFactory.factory().domain(bn.getDomain(A, C)).data(new double[]{.4, .6, .6, .4}).get();

		bn.setFactors(factors);

		assertArrayEquals(new int[]{B, C}, bn.getChildren(A));
		assertArrayEquals(new int[]{A}, bn.getParents(C));
		assertArrayEquals(new int[]{A}, bn.getParents(B));

		Int2IntMap obs = new Int2IntOpenHashMap();
		BayesianFactor q;

		final BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>();

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

		obs = new Int2IntOpenHashMap();

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
		final BayesianNetwork bn = new BayesianNetwork();
		final int A = bn.addVariable(2);
		final int B = bn.addVariable(2);
		final int C = bn.addVariable(2);
		final int D = bn.addVariable(2);

		bn.addParent(B, A);
		bn.addParent(C, B);
		bn.addParent(D, C);

		final BayesianFactor[] factors = new BayesianFactor[bn.getVariables().length];
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
		final BeliefPropagation<BayesianFactor> inf = new BeliefPropagation<>();

		final Int2IntMap obs = new Int2IntOpenHashMap();
		obs.put(D, 0);
		final BayesianFactor q = inf.query(bn, obs, A);
		System.out.println("query=" + q);

		assertEquals(res, q);
	}

	@Test
	public void testAlloyNumberOfFactorsPerClique() throws IOException {
		final BayesianNetwork model = BIFParser.read("models/bif/alloy.bif").network;
		final BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(false);

		try {
			bp.query(model, 0);
		} catch (OutOfMemoryError ignored) {
			// this will raise an OutOfMemoryError that we can safely ignore
		}

		final long factors = bp.potentialsPerClique.values().stream()
				.mapToLong(Set::size)
				.sum();

		assertEquals(model.getVariables().length, factors);

		bp.potentialsPerClique.forEach((c, f) -> {
			assertNotNull(f);
			assertNotNull(c);
			assertTrue(f.size() > 0);
		});
	}

	@Test
	void testVariableElimination() {
		final BayesianNetwork model = BayesianNetworkContainer.mix5Variables().network;

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{4, 3, 1, 0, 2});
		final BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(false);

		Int2IntMap evidence;
		BayesianFactor Qlbp;
		BayesianFactor Qve;

		evidence = new Int2IntOpenHashMap();
		Qlbp = bp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("BP: P(Rain) =                                     " + Qlbp);
		System.out.println("VE: P(Rain) =                                     " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap();
		evidence.put(3, 0);
		evidence.put(4, 1);
		Qlbp = bp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("BP: P(Rain|Wet Grass = false, Slippery = true) =  " + Qlbp);
		System.out.println("VE: P(Rain|Wet Grass = false, Slippery = true) =  " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.05);

		evidence = new Int2IntOpenHashMap();
		evidence.put(3, 0);
		evidence.put(4, 0);
		Qlbp = bp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("BP: P(Rain|Wet Grass = false, Slippery = false) = " + Qlbp);
		System.out.println("VE: P(Rain|Wet Grass = false, Slippery = false) = " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap();
		evidence.put(0, 1);
		Qlbp = bp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("BP: P(Rain|Winter = true) =                       " + Qlbp);
		System.out.println("VE: P(Rain|Winter = true) =                       " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap();
		evidence.put(0, 0);
		Qlbp = bp.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("BP: P(Rain|Winter = false) =                      " + Qlbp);
		System.out.println("VE: P(Rain|Winter = false) =                      " + Qve);

		assertEquals(Qlbp.getValue(0), Qve.getValue(0), 0.01);
	}
}