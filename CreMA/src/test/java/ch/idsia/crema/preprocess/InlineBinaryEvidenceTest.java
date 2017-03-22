package ch.idsia.crema.preprocess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class InlineBinaryEvidenceTest {

	@Test
	public void test1_0() {

		for (int i = 0; i < 3; ++i) {
			SparseModel<GenericFactor> model = new SparseModel<>();

			int n0 = model.addVariable(3);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n0), model.getDomain());
			f0.set(new double[] { 0.2, 0.7, 0.1 }, new double[] { 0.3, 0.8, 0.5 });
			model.setFactor(n0, f0);

			TIntIntMap evidence = new TIntIntHashMap();
			evidence.put(n0, i);

			BinarizeEvidence bin = new BinarizeEvidence();
			model = bin.execute(model, evidence, 2, false);
			int ev = bin.getLeafDummy();
			
			BayesianFactor f = (BayesianFactor) model.getFactor(ev);

			assertTrue(ev > n0);
			assertArrayEquals(new int[] { n0, ev }, f.getDomain().getVariables());

			double[] expect = new double[] { 1, 1, 1, 0, 0, 0 };
			expect[i] = 0;
			expect[i + 3] = 1;

			assertArrayEquals("not as expected: " + i, expect, f.getData(), 0);
		}
	}

	@Test
	public void test2() {
		SparseModel<GenericFactor> model = new SparseModel<>();

		TIntIntMap evidence = new TIntIntHashMap();

		for (int i = 0; i < 2; ++i) {
			int n = model.addVariable(3);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n), model.getDomain());
			f0.set(new double[] { 0.2, 0.7, 0.1 }, new double[] { 0.3, 0.8, 0.5 });
			model.setFactor(n, f0);

			evidence.put(n, 0);
		}

		BinarizeEvidence bin = new BinarizeEvidence();
		model = bin.execute(model, evidence, 2, false);
		int ev = bin.getLeafDummy();
		
		BayesianFactor f = (BayesianFactor) model.getFactor(ev);

		assertTrue(ev == 2);

		assertArrayEquals(new double[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, f.getData(), 0);
	}

	/**
	 * 2 vars observed on state 1
	 */
	@Test
	public void test2_1() {
		SparseModel<GenericFactor> model = new SparseModel<>();

		TIntIntMap evidence = new TIntIntHashMap();

		for (int i = 0; i < 2; ++i) {
			int n = model.addVariable(3);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n), model.getDomain());
			f0.set(new double[] { 0.2, 0.7, 0.1 }, new double[] { 0.3, 0.8, 0.5 });
			model.setFactor(n, f0);

			evidence.put(n, 1);
		}

		BinarizeEvidence bin = new BinarizeEvidence();
		model = bin.execute(model, evidence, 2, false);
		int ev = bin.getLeafDummy();
		
		BayesianFactor f = (BayesianFactor) model.getFactor(ev);

		assertTrue(ev == 2);

		assertArrayEquals(new double[] { 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0 }, f.getData(), 0);
	}

	/**
	 * 3 vars observed on state 0
	 */
	@Test
	public void test3() {
		SparseModel<GenericFactor> model = new SparseModel<>();

		TIntIntMap evidence = new TIntIntHashMap();

		for (int i = 0; i < 3; ++i) {
			int n = model.addVariable(2);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n), model.getDomain());
			f0.set(new double[] { 0.2, 0.7, 0.1 }, new double[] { 0.3, 0.8, 0.5 });
			model.setFactor(n, f0);

			evidence.put(n, 0);
		}

		BinarizeEvidence bin = new BinarizeEvidence();
		model = bin.execute(model, evidence, 2, false);
		int ev = bin.getLeafDummy();
		
		BayesianFactor f = (BayesianFactor) model.getFactor(ev);

		assertEquals(4, ev);

		assertArrayEquals(new double[] { 1, 1, 0, 1, 0, 0, 1, 0 }, f.getData(), 0);

		BayesianFactor f2 = (BayesianFactor) model.getFactor(ev - 1);
		assertArrayEquals(new double[] { 0, 1, 1, 1, 1, 0, 0, 0 }, f2.getData(), 0);
	}

}
