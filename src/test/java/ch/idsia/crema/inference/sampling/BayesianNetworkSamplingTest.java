package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    05.10.2021 09:52
 */
class BayesianNetworkSamplingTest {

	@BeforeEach
	void setUp() {
		final Random r = new Random(42);
		RandomUtil.setRandom(r);
	}

	@Test
	void oneParentTest() {
		final DAGModel<BayesianFactor> model = new DAGModel<>();

		final int A = model.addVariable(2);
		final int B = model.addVariable(2);
		final int C = model.addVariable(2);

		model.addParents(B, A);
		model.addParents(C, A);

		model.setFactors(new BayesianFactor[]{
				BayesianFactorFactory.factory().domain(model.getDomain(A)).data(new double[]{.5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(B, A)).data(new double[]{.4, .6, .5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(C, A)).data(new double[]{.5, .5, .4, .6}).get(),
		});

		final BayesianNetworkSampling bns = new BayesianNetworkSampling();

		TIntIntMap sample;
		sample = bns.sample(model);

		Assertions.assertEquals(3, sample.size());
		Assertions.assertTrue(sample.containsKey(A));
		Assertions.assertTrue(sample.containsKey(B));
		Assertions.assertTrue(sample.containsKey(C));

		sample = bns.sample(model, B, C);

		Assertions.assertEquals(2, sample.size());
		Assertions.assertFalse(sample.containsKey(A));
		Assertions.assertTrue(sample.containsKey(B));
		Assertions.assertTrue(sample.containsKey(C));
	}

	@Test
	void multipleParentTest() {
		final DAGModel<BayesianFactor> model = new DAGModel<>();

		final int A = model.addVariable(2);
		final int B = model.addVariable(2);
		final int C = model.addVariable(2);
		final int D = model.addVariable(2);

		model.addParents(C, A);
		model.addParents(D, A, B);

		model.setFactors(new BayesianFactor[]{
				BayesianFactorFactory.factory().domain(model.getDomain(A)).data(new double[]{.5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(B)).data(new double[]{.5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(C, A)).data(new double[]{.4, .6, .5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(D, A, B)).data(new double[]{.8, .4, .3, .6, .2, .6, .7, .4}).get(),
		});

		final BayesianNetworkSampling bns = new BayesianNetworkSampling();

		TIntIntMap sample;
		sample = bns.sample(model);

		Assertions.assertEquals(4, sample.size());
		Assertions.assertTrue(sample.containsKey(A));
		Assertions.assertTrue(sample.containsKey(B));
		Assertions.assertTrue(sample.containsKey(C));
		Assertions.assertTrue(sample.containsKey(D));

		sample = bns.sample(model, A, B);

		Assertions.assertEquals(2, sample.size());
		Assertions.assertTrue(sample.containsKey(A));
		Assertions.assertTrue(sample.containsKey(B));
		Assertions.assertFalse(sample.containsKey(C));
		Assertions.assertFalse(sample.containsKey(D));
	}


	@Test
	void observedParentTest() {
		final DAGModel<BayesianFactor> model = new DAGModel<>();

		final int A = model.addVariable(2);
		final int B = model.addVariable(2);
		final int C = model.addVariable(2);

		model.addParents(B, A);
		model.addParents(C, A);

		model.setFactors(new BayesianFactor[]{
				BayesianFactorFactory.factory().domain(model.getDomain(A)).data(new double[]{.5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(B, A)).data(new double[]{.4, .6, .5, .5}).get(),
				BayesianFactorFactory.factory().domain(model.getDomain(C, A)).data(new double[]{.5, .5, .4, .6}).get(),
		});

		final BayesianNetworkSampling bns = new BayesianNetworkSampling();

		final TIntIntMap obs = new TIntIntHashMap();
		obs.put(A, 0);

		double pA = 0;
		double pB = 0;
		double pC = 0;
		for (int i = 0; i < 100; i++) {
			final TIntIntMap sample = bns.sample(model, obs);

			pA += sample.get(A) / 100.0;
			pB += sample.get(B) / 100.0;
			pC += sample.get(C) / 100.0;
		}

		Assertions.assertEquals(0.0, pA, 1e-3);
		Assertions.assertEquals(0.69, pB, 1e-2);
		Assertions.assertEquals(0.54, pC, 1e-2);
	}
}