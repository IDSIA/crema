package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.BayesianNetworkContainer;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:05
 */
public class LogicSamplingTest {

	BayesianNetwork model;
	LogicSampling ls;

	@BeforeEach
	public void setUp() {
		BayesianNetworkContainer BN = BayesianNetworkContainer.mix5Variables();

		model = BN.network;
		ls = new LogicSampling();
		ls.setPreprocess(false);

		RandomUtil.setRandomSeed(0);
	}

	@Test
	public void testSamplingRaiseException() {
		ls.setIterations(10000);

		TIntIntMap evidence;
		System.out.println("P(Rain) =                                     " + ls.query(model, 2));

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1});
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + ls.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0});
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + ls.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{1});
		System.out.println("P(Rain|Winter = true) =                       " + ls.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{0});
		System.out.println("P(Rain|Winter = false) =                      " + ls.query(model, evidence, 2));
	}

	@Test
	void testLogicSampling() {
		final BayesianNetwork model = new BayesianNetwork();

		final int M = model.addVariable(2);
		final int S = model.addVariable(2);
		final int B = model.addVariable(2);
		final int C = model.addVariable(2);
		final int H = model.addVariable(2);

		model.addParent(S, M);
		model.addParent(B, M);
		model.addParent(C, B);
		model.addParent(C, S);
		model.addParent(H, B);

		model.setFactor(M, new BayesianFactor(model.getDomain(M), new double[]{.9, .1}));
		model.setFactor(S, new BayesianFactor(model.getDomain(M, S), new double[]{.2, .05, .8, .95}));
		model.setFactor(B, new BayesianFactor(model.getDomain(M, B), new double[]{.8, .2, .2, .8}));
		model.setFactor(C, new BayesianFactor(model.getDomain(S, B, C), new double[]{.8, .8, .8, .05, .2, .2, .2, .95}));
		model.setFactor(H, new BayesianFactor(model.getDomain(H, B), new double[]{.8, .6, .2, .4}));


		int[] its = {1, 10, 100, 1000, 10000, 100000};

		final TIntIntMap ev = new TIntIntHashMap();
		ev.put(B, 0);

		for (int it : its) {
			ls.setIterations(it);

			final BayesianFactor q = ls.query(model, ev, M);
			System.out.println(q);
		}

	}
}