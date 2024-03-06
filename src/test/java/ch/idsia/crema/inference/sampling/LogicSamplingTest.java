package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.RandomUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
		final double delta = .01;

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{4, 3, 1, 0, 2});
		ls.setIterations(10000);

		Int2IntMap evidence;
		BayesianFactor Qls;
		BayesianFactor Qve;

		evidence = new Int2IntOpenHashMap();
		Qls = ls.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LS: P(Rain) =                                     " + Qls);
		System.out.println("VE: P(Rain) =                                     " + Qve);

		assertEquals(Qls.getValue(0), Qve.getValue(0), delta);

/*
		// TODO this does not work and I don't know why...
		evidence = new Int2IntOpenHashMap(new int[]{3, 4}, new int[]{0, 1});
		Qls = ls.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LS: P(Rain|Wet Grass = false, Slippery = true) =  " + Qls);
		System.out.println("VE: P(Rain|Wet Grass = false, Slippery = true) =  " + Qve);

		final BayesianFactor f = model.getFactor(0)
				.combine(model.getFactor(1))
				.combine(model.getFactor(2))
				.combine(model.getFactor(3))
				.combine(model.getFactor(4))
				.filter(evidence)
				.marginalize(0, 1, 3, 4)
				.normalize();

		System.out.println("join: " + f);

		assertEquals(Qls.getValue(0), Qve.getValue(0), delta);

		evidence = new Int2IntOpenHashMap(new int[]{3, 4}, new int[]{0, 0});
		Qls = ls.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LS: P(Rain|Wet Grass = false, Slippery = false) = " + Qls);
		System.out.println("VE: P(Rain|Wet Grass = false, Slippery = false) = " + Qve);

		assertEquals(Qls.getValue(0), Qve.getValue(0), delta);

*/

		evidence = new Int2IntOpenHashMap(new int[]{0}, new int[]{1});
		Qls = ls.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LS: P(Rain|Winter = true) =                       " + Qls);
		System.out.println("VE: P(Rain|Winter = true) =                       " + Qve);

		assertEquals(Qls.getValue(0), Qve.getValue(0), delta);

		evidence = new Int2IntOpenHashMap(new int[]{0}, new int[]{0});
		Qls = ls.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LS: P(Rain|Winter = false) =                      " + Qls);
		System.out.println("VE: P(Rain|Winter = false) =                      " + Qve);

		assertEquals(Qls.getValue(0), Qve.getValue(0), delta);

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

		model.setFactor(M, BayesianFactorFactory.factory().domain(model.getDomain(M)).data(new double[]{.9, .1}).get());
		model.setFactor(S, BayesianFactorFactory.factory().domain(model.getDomain(M, S)).data(new double[]{.2, .05, .8, .95}).get());
		model.setFactor(B, BayesianFactorFactory.factory().domain(model.getDomain(M, B)).data(new double[]{.8, .2, .2, .8}).get());
		model.setFactor(C, BayesianFactorFactory.factory().domain(model.getDomain(S, B, C)).data(new double[]{.8, .8, .8, .05, .2, .2, .2, .95}).get());
		model.setFactor(H, BayesianFactorFactory.factory().domain(model.getDomain(H, B)).data(new double[]{.8, .6, .2, .4}).get());


		int[] its = {1, 10, 100, 1000, 10000, 100000};

		final Int2IntMap ev = new Int2IntOpenHashMap();
		ev.put(B, 0);

		for (int it : its) {
			ls.setIterations(it);

			final BayesianFactor q = ls.query(model, ev, M);
			System.out.println(q);
		}

	}
}