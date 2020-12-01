package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.12.2020 10:21
 */
public class FrequentistEMTest {

	@Test
	public void testLearningFromSimpleData() throws InterruptedException {
		// https://www.cse.ust.hk/bnbook/pdf/l07.h.pdf
		BayesianNetwork model = new BayesianNetwork();

		for (int i = 0; i < 3; i++)
			model.addVariable(2);

		int[] X = model.getVariables();

		model.addParent(X[1], X[0]);
		model.addParent(X[2], X[1]);

		model.setFactor(X[0], new BayesianFactor(model.getDomain(X[0]), new double[]{0.5, 0.5}));
		model.setFactor(X[1], new BayesianFactor(model.getDomain(X[1], X[0]), new double[]{2. / 3, 1. / 3, 1. / 3, 2. / 3}));
		model.setFactor(X[2], new BayesianFactor(model.getDomain(X[2], X[1]), new double[]{2. / 3, 1. / 3, 1. / 3, 2. / 3}));

		int[][] dataX = {
				{0, 0, 0},
				{1, 1, 1},
				{0, -1, 0},
				{1, -1, 1}
		};

		TIntIntMap[] observations = new TIntIntMap[dataX.length];
		for (int i = 0; i < observations.length; i++) {
			observations[i] = new TIntIntHashMap();
			for (int j = 0; j < dataX[i].length; j++) {
				if (dataX[i][j] >= 0)
					observations[i].put(X[j], dataX[i][j]);
			}
		}

		//RandomUtil.setRandomSeed(222);
		//model = (BayesianNetwork) BayesianFactor.randomModel(model, 4, false);

		ExpectationMaximization<BayesianFactor> inf = new FrequentistEM(model)
				.setRegularization(0.0)
				.setInline(false);

		inf.run(Arrays.asList(observations), 100);

		// Posterior
		assertArrayEquals(new double[]{.5, .5}, inf.getPosterior().getFactor(X[0]).getData(), 1e-6); //[0.5, 0.5]
		// P(X1|X0=0)
		assertArrayEquals(new double[]{2. / 3., 1. / 3.}, inf.getPosterior().getFactor(X[1]).filter(X[0], 0).getData(), 1e-6); // [0.9, 0.1]
		// P(X1|X0=1)
		assertArrayEquals(new double[]{1. / 3., 2. / 3.}, inf.getPosterior().getFactor(X[1]).filter(X[0], 1).getData(), 1e-6); // [0.1, 0.9]
		// P(X2|X1=0)
		assertArrayEquals(new double[]{2. / 3., 1. / 3.}, inf.getPosterior().getFactor(X[2]).filter(X[1], 0).getData(), 1e-6); // [0.9, 0.1]
		// P(X2|X1=1)
		assertArrayEquals(new double[]{1. / 3., 2. / 3.}, inf.getPosterior().getFactor(X[2]).filter(X[1], 1).getData(), 1e-6); // [0.1, 0.9]
	}

}