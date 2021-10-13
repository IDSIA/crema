package ch.idsia.crema.entropy;

import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import org.junit.jupiter.api.Test;

import static ch.idsia.crema.entropy.BayesianEntropy.H;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.12.2020 11:15
 */
public class BayesianEntropyTest {

	@Test
	public void testEntropySingleFactorHigh() {
		BayesianFactor A = new BayesianDefaultFactor(new int[]{1}, new int[]{4}, new double[]{.25, .25, .25, .25});

		double h = H(A);

		assertEquals(1, h, 1e-3);
	}

	@Test
	public void testEntropySingleFactorLow() {
		BayesianFactor A = new BayesianDefaultFactor(new int[]{1}, new int[]{2}, new double[]{1e-6, 1 - 1e-6});

		double h = H(A);

		assertEquals(0, h, .1);
	}

	@Test
	public void testEntropyConditionedHigh() {
		BayesianFactor A = new BayesianDefaultFactor(new int[]{1}, new int[]{2}, new double[]{.5, .5});
		BayesianFactor B = new BayesianDefaultFactor(new int[]{1, 2}, new int[]{2, 2}, new double[]{.5, .5, .5, .5});

		double h = H(A, B);

		assertEquals(1, h, 1e-3);
	}

	@Test
	public void testEntropyConditionedLow() {
		BayesianFactor A = new BayesianDefaultFactor(new int[]{1}, new int[]{2}, new double[]{1e-6, 1 - 1e-6});
		BayesianFactor B = new BayesianDefaultFactor(new int[]{1, 2}, new int[]{2, 2}, new double[]{1e-6, 1 - 1e-6, 1 - 1e-6, 1e-6});

		double h = H(A, B);

		assertEquals(0, h, 1e-3);
	}

	@Test
	void testEntropyDeterministic() {
		BayesianFactor f1 = new BayesianDefaultFactor(new int[]{1}, new int[]{2}, new double[]{0., 1.});
		BayesianFactor f2 = new BayesianDefaultFactor(new int[]{1}, new int[]{3}, new double[]{0., 1., 0.});
		BayesianFactor f3 = new BayesianDefaultFactor(new int[]{1}, new int[]{4}, new double[]{0., 1., 0., 0.});
		BayesianFactor f4 = new BayesianDefaultFactor(new int[]{1}, new int[]{4}, new double[]{0., .5, .5, 0.});

		double h1 = H(f1);
		double h2 = H(f2);
		double h3 = H(f3);
		double h4 = H(f4);

		// adding .0 to avoid -0.0 numerical issue
		assertEquals(.0, h1, 1e-6);
		assertEquals(.0, h2, 1e-6);
		assertEquals(.0, h3, 1e-6);
		assertEquals(.5, h4, 1e-3);
	}
}