package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.MixedModel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InlineBinaryEvidenceTest {

	@Test
	public void test1_0() {
		for (int i = 0; i < 3; ++i) {
			DAGModel<IntervalFactor> model = new DAGModel<>();

			int n0 = model.addVariable(3);

			IntervalFactor f0 = IntervalFactorFactory.factory()
					.domain(model.getDomain(n0), model.getDomain())
					.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5})
					.get();
			model.setFactor(n0, f0);

			Int2IntMap evidence = new Int2IntOpenHashMap();
			evidence.put(n0, i);

			BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
			bin.setSize(2);
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

		Int2IntMap evidence = new Int2IntOpenHashMap();

		for (int i = 0; i < 2; ++i) {
			int n = model.addVariable(3);

			IntervalFactor f0 = IntervalFactorFactory.factory()
					.domain(model.getDomain(n), model.getDomain())
					.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5})
					.get();
			model.setFactor(n, f0);

			evidence.put(n, 0);
		}

		BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
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

		Int2IntMap evidence = new Int2IntOpenHashMap();

		for (int i = 0; i < 2; ++i) {
			int n = model.addVariable(3);

			IntervalFactor f0 = IntervalFactorFactory.factory()
					.domain(model.getDomain(n), model.getDomain())
					.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5})
					.get();
			model.setFactor(n, f0);

			evidence.put(n, 1);
		}

		BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
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

		Int2IntMap evidence = new Int2IntOpenHashMap();

		for (int i = 0; i < 3; ++i) {
			int n = model.addVariable(2);

			IntervalFactor f0 = IntervalFactorFactory.factory()
					.domain(model.getDomain(n), model.getDomain())
					.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5})
					.get();
			model.setFactor(n, f0);

			evidence.put(n, 0);
		}

		BinarizeEvidence<IntervalFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
		MixedModel mixedModel = bin.execute(model, evidence);
		int ev = bin.getEvidenceNode();

		BayesianFactor f = mixedModel.getConvertedFactor(BayesianFactor.class, ev);

		assertEquals(4, ev);

		assertArrayEquals(new double[]{1, 1, 0, 1, 0, 0, 1, 0}, f.getData(), 1e-9);

		BayesianFactor f2 = mixedModel.getConvertedFactor(BayesianFactor.class, ev - 1);
		assertArrayEquals(new double[]{0, 1, 1, 1, 1, 0, 0, 0}, f2.getData(), 1e-9);
	}

}
