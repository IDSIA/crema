package ch.idsia.crema.inference.ve;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.apache.commons.math3.util.MathArrays;
import org.junit.Test;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.core.DomainBuilder;
import gnu.trove.map.hash.TIntIntHashMap;

public class VariableEliminationTest {

	@Test
	public void testInference1() {
		// [[0.6, 0.4, 0.5, 0.5, 0.2, 0.8], [0.19999999999999996, 0.8, 0.6, 0.4,
		// 0.30000000000000004, 0.7], [0.09999999999900003, 0.900000000001, 0.3,
		// 0.7, 0.19999999999999996, 0.8, 0.7, 0.30000000000000004], [0.2, 0.7,
		// 0.1]]
		BayesianFactor[] f = new BayesianFactor[4];

		f[0] = new BayesianFactor(DomainBuilder.var(0, 1, 2).size(2, 2, 2), false);
		f[1] = new BayesianFactor(DomainBuilder.var(1, 3).size(2, 2), false);
		f[2] = new BayesianFactor(DomainBuilder.var(2, 3).size(2, 2), false);
		f[3] = new BayesianFactor(DomainBuilder.var(3).size(2), false);
		// f[4] = new BayesianFactor(DomainBuilder.var(0, 4).size(2, 2),
		// false);

		f[3].setData(new double[] { 0.3, 0.7 });
		f[1].setData(new double[] { 0.4, 0.6, 0.5, 0.5 });
		f[2].setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		f[0].setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7 });

		int[] seq = new int[] { 0, 1, 2, 3 };

		for (int i = 0; i < 50; ++i) {
			MathArrays.shuffle(seq);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			ve.setFactors(f);

			BayesianFactor fa = ve.run(0);
			assertArrayEquals(new double[] { 0.4678, 0.5322 }, fa.getData(), 0.0000000000001);

		}
	}

	/**
	 * Test variable elimination when the network has useless nodes In this
	 * example during the inferences not all nodes are relevant. Remove barren
	 * method would have removed them.
	 */
	@Test
	public void testInferenceNotBarren() {
		BayesianFactor[] f = new BayesianFactor[4];

		f[0] = new BayesianFactor(DomainBuilder.var(0, 1, 2).size(2, 2, 2), false);
		f[1] = new BayesianFactor(DomainBuilder.var(1, 3).size(2, 2), false);
		f[2] = new BayesianFactor(DomainBuilder.var(2, 3).size(2, 2), false);
		f[3] = new BayesianFactor(DomainBuilder.var(3).size(2), false);

		f[3].setData(new double[] { 0.3, 0.7 });
		f[1].setData(new double[] { 0.4, 0.6, 0.5, 0.5 });
		f[2].setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		f[0].setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7 });

		int[] seq = new int[] { 0, 1, 2, 3 };

		MathArrays.shuffle(seq);

		VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
		ve.setFactors(f);

		BayesianFactor fa = ve.run(1);
		// 0.47, 0.53
		assertArrayEquals(new double[] { 0.4 * 0.3 + 0.5 * 0.7, 0.6 * 0.3 + 0.5 * 0.7 }, fa.getData(), 0.0000000000001);

		ve = new FactorVariableElimination<>(seq);
		ve.setFactors(f);
		fa = ve.run(2);
		assertArrayEquals(new double[] { 0.7 * 0.3 + 0.1 * 0.7, 0.3 * 0.3 + 0.9 * 0.7 }, fa.getData(), 0.0000000000001);

	}

	/**
	 * Inference on a single factor (with single variable domain)
	 */
	@Test
	public void testInferenceLoneFactor() {
		BayesianFactor f = new BayesianFactor(DomainBuilder.var(3).size(2), false);

		f.setData(new double[] { 0.3, 0.7 });

		int[] seq = new int[] { 3 };

		VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
		ve.setFactors(Arrays.asList(f));

		BayesianFactor fa = ve.run(3);
		assertArrayEquals(new double[] { 0.3, 0.7 }, fa.getData(), 0);

	}

	/**
	 * Inference on a single factor (with single variable domain)
	 */
	@Test
	public void testInferenceMultiple() {
		BayesianFactor[] f = new BayesianFactor[4];

		f[0] = new BayesianFactor(DomainBuilder.var(0, 1, 2).size(2, 2, 2), false);
		f[1] = new BayesianFactor(DomainBuilder.var(1, 3).size(2, 2), false);
		f[2] = new BayesianFactor(DomainBuilder.var(2, 3).size(2, 2), false);
		f[3] = new BayesianFactor(DomainBuilder.var(3).size(2), false);

		f[3].setData(new double[] { 0.3, 0.7 });
		f[1].setData(new double[] { 0.4, 0.6, 0.5, 0.5 });
		f[2].setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		f[0].setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7 });

		int[] seq = new int[] { 0, 1, 2, 3 };

		VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
		ve.setFactors(f);

		// all combined
		BayesianFactor t1 = f[3].combine(f[1]).combine(f[2]).combine(f[0]);
		BayesianFactor t3 = t1.marginalize(1).marginalize(2);

		BayesianFactor fa = ve.run(0, 3);
		assertArrayEquals(t3.getData(), fa.getData(), 0.000000000001);

	}

	/**
	 * Posterior computation
	 */
	@Test
	public void testInferencePosterior() {
		BayesianFactor[] f = new BayesianFactor[4];

		f[0] = new BayesianFactor(DomainBuilder.var(0, 1, 2).size(2, 2, 2), false);
		f[1] = new BayesianFactor(DomainBuilder.var(1, 3).size(2, 2), false);
		f[2] = new BayesianFactor(DomainBuilder.var(2, 3).size(2, 2), false);
		f[3] = new BayesianFactor(DomainBuilder.var(3).size(2), false);
		// f[4] = new BayesianFactor(DomainBuilder.var(0, 3).size(2, 2),
		// false);

		f[3].setData(new double[] { 0.3, 0.7 });
		f[1].setData(new double[] { 0.4, 0.6, 0.5, 0.5 });
		f[2].setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		f[0].setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7 });
		// f[4].setData(new double[] { 0, 1, 1, 0 });

		int[] seq = new int[] { 0, 1, 2, 3 };

		MathArrays.shuffle(seq);

		VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
		ve.setFactors(f);

		ve.setEvidence(new TIntIntHashMap(new int[] { 0 }, new int[] { 0 }));

		BayesianFactor fa = ve.run(3);

		assertArrayEquals(new double[] { 0.2218896964518944, 0.7781103035492434 }, fa.getData(), 0.00000000001);
	}
}
