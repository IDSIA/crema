package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.graphical.BayesianNetwork;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.12.2020 10:21
 */
public class FrequentistEMTest {

	@Test
	public void testModelLoading() throws InterruptedException {
		// https://www.cse.ust.hk/bnbook/pdf/l07.h.pdf
		BayesianNetwork model = new BayesianNetwork();

		for (int i = 0; i < 3; i++)
			model.addVariable(2);

		int[] X = model.getVariables();

		model.addParent(X[1], X[0]);
		model.addParent(X[2], X[1]);

		model.setFactor(X[0], BayesianFactorFactory.factory().domain(model.getDomain(X[0])).data(new double[]{0.5, 0.5}).get());
		model.setFactor(X[1], BayesianFactorFactory.factory().domain(model.getDomain(X[1], X[0])).data(new double[]{2. / 3, 1. / 3, 1. / 3, 2. / 3}).get());
		model.setFactor(X[2], BayesianFactorFactory.factory().domain(model.getDomain(X[2], X[1])).data(new double[]{2. / 3, 1. / 3, 1. / 3, 2. / 3}).get());

		int[][] dataX = {
				{0, 0, 0},
				{1, 1, 1},
				{0, -1, 0},
				{1, -1, 1}
		};

		Int2IntMap[] observations = new Int2IntMap[dataX.length];
		for (int i = 0; i < observations.length; i++) {
			observations[i] = new Int2IntOpenHashMap();
			for (int j = 0; j < dataX[i].length; j++) {
				if (dataX[i][j] >= 0)
					observations[i].put(X[j], dataX[i][j]);
			}
		}

		ExpectationMaximization<BayesianFactor> inf = new FrequentistEM(model)
				.setRegularization(0.0)
				.setInline(false);

		// after one iteration
		inf.run(Arrays.asList(observations), 1);

		// Posterior
		assertArrayEquals(new double[]{.5, .5}, inf.getPosterior().getFactor(X[0]).getData(), 1e-9); //[0.5, 0.5]
		// P(X1|X0=0)
		assertArrayEquals(new double[]{.9, .1}, inf.getPosterior().getFactor(X[1]).filter(X[0], 0).getData(), 1e-9); // [0.9, 0.1]
		// P(X1|X0=1)
		assertArrayEquals(new double[]{.1, .9}, inf.getPosterior().getFactor(X[1]).filter(X[0], 1).getData(), 1e-9); // [0.1, 0.9]
		// P(X2|X1=0)
		assertArrayEquals(new double[]{.9, .1}, inf.getPosterior().getFactor(X[2]).filter(X[1], 0).getData(), 1e-9); // [0.9, 0.1]
		// P(X2|X1=1)
		assertArrayEquals(new double[]{.1, .9}, inf.getPosterior().getFactor(X[2]).filter(X[1], 1).getData(), 1e-9); // [0.1, 0.9]

		// after convergence
		inf.run(Arrays.asList(observations), 100);

		// Posterior
		assertArrayEquals(new double[]{.5, .5}, inf.getPosterior().getFactor(X[0]).getData(), 1e-6); //[0.5, 0.5]
		// P(X1|X0=0)
		assertArrayEquals(new double[]{1., .0}, inf.getPosterior().getFactor(X[1]).filter(X[0], 0).getData(), 1e-6); // [0.9, 0.1]
		// P(X1|X0=1)
		assertArrayEquals(new double[]{.0, 1.}, inf.getPosterior().getFactor(X[1]).filter(X[0], 1).getData(), 1e-6); // [0.1, 0.9]
		// P(X2|X1=0)
		assertArrayEquals(new double[]{1., .0}, inf.getPosterior().getFactor(X[2]).filter(X[1], 0).getData(), 1e-6); // [0.9, 0.1]
		// P(X2|X1=1)
		assertArrayEquals(new double[]{.0, 1.}, inf.getPosterior().getFactor(X[2]).filter(X[1], 1).getData(), 1e-6); // [0.1, 0.9]
	}

}