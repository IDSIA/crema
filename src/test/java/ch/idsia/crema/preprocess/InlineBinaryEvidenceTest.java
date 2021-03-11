package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.MixedModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InlineBinaryEvidenceTest {

	@Test
	public void test1_0() {
		for (int i = 0; i < 3; ++i) {
			DAGModel<IntervalFactor> model = new DAGModel<>();

			int n0 = model.addVariable(3);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n0), model.getDomain());
			f0.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5});
			model.setFactor(n0, f0);

			TIntIntMap evidence = new TIntIntHashMap();
			evidence.put(n0, i);

			BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
			bin.setSize(2);
			bin.setLog(false);
			MixedModel mixedModel = bin.execute(model, evidence);
			int ev = bin.getEvidenceNode();

			BayesianFactor f = mixedModel.getConvertedFactor(BayesianFactor.class, ev);

			assertTrue(ev > n0);
			assertArrayEquals(new int[]{n0, ev}, f.getDomain().getVariables());

			double[] expect = new double[]{1, 1, 1, 0, 0, 0};
			expect[i] = 0;
			expect[i + 3] = 1;

			assertArrayEquals(expect, f.getData(), 1e-9, "not as expected: " + i);
		}
	}

	@Test
	public void test2() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		TIntIntMap evidence = new TIntIntHashMap();

		for (int i = 0; i < 2; ++i) {
			int n = model.addVariable(3);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n), model.getDomain());
			f0.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5});
			model.setFactor(n, f0);

			evidence.put(n, 0);
		}

		BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
		bin.setLog(false);
		MixedModel mixedModel = bin.execute(model, evidence);
		int ev = bin.getEvidenceNode();

		BayesianFactor f = mixedModel.getConvertedFactor(BayesianFactor.class, ev);

		assertEquals(2, ev);

		assertArrayEquals(new double[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, f.getData(), 1e-9);
	}

	/**
	 * 2 vars observed on state 1
	 */
	@Test
	public void test2_1() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		TIntIntMap evidence = new TIntIntHashMap();

		for (int i = 0; i < 2; ++i) {
			int n = model.addVariable(3);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n), model.getDomain());
			f0.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5});
			model.setFactor(n, f0);

			evidence.put(n, 1);
		}

		BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
		bin.setLog(false);
		MixedModel mixedModel = bin.execute(model, evidence);
		int ev = bin.getEvidenceNode();

		BayesianFactor f = mixedModel.getConvertedFactor(BayesianFactor.class, ev);

		assertEquals(2, ev);

		assertArrayEquals(new double[]{1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0}, f.getData(), 1e-9);
	}

	/**
	 * 3 vars observed on state 0
	 */
	@Test
	public void test3() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		TIntIntMap evidence = new TIntIntHashMap();

		for (int i = 0; i < 3; ++i) {
			int n = model.addVariable(2);

			IntervalFactor f0 = new IntervalFactor(model.getDomain(n), model.getDomain());
			f0.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5});
			model.setFactor(n, f0);

			evidence.put(n, 0);
		}

		BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
		bin.setLog(false);
		MixedModel mixedModel = bin.execute(model, evidence);
		int ev = bin.getEvidenceNode();

		BayesianFactor f = mixedModel.getConvertedFactor(BayesianFactor.class, ev);

		assertEquals(4, ev);

		assertArrayEquals(new double[]{1, 1, 0, 1, 0, 0, 1, 0}, f.getData(), 1e-9);

		BayesianFactor f2 = mixedModel.getConvertedFactor(BayesianFactor.class, ev - 1);
		assertArrayEquals(new double[]{0, 1, 1, 1, 1, 0, 0, 0}, f2.getData(), 1e-9);
	}

}
