package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.RandomUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:05
 */
public class LikelihoodWeightingSamplingTest {

	LikelihoodWeightingSampling lws;

	@BeforeEach
	public void setUp() {
		lws = new LikelihoodWeightingSampling(100000, false );
		RandomUtil.setRandomSeed(0);
	}

	@Test
	public void vsVariableElimination1() {
		BayesianNetwork model = BayesianNetworkContainer.mix5Variables().network;

		Int2IntMap evidence = new Int2IntOpenHashMap(new int[]{3}, new int[]{0});

		for (int query = 0; query < 5; query++) {
			BayesianFactor resLWS = lws.query(model, evidence, query);

			MinFillOrdering ordering = new MinFillOrdering();
			int[] seq = ordering.apply(model);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			BayesianFactor resVE = ve.query(model, evidence, query);

			double distance0 = resLWS.getData()[0] - resVE.getData()[0];

			System.out.println("LWS: " + resLWS + " " + Arrays.toString(resLWS.getData()));
			System.out.println("VE:  " + resVE + " " + Arrays.toString(resVE.getData()));
			System.out.println(distance0 + " ");
		}
	}

	@Test
	public void vsVariableElimination2() {
		BayesianNetwork model = BayesianNetworkContainer.mix5Variables().network;

		VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{4, 3, 1, 0, 2});

		Int2IntMap evidence;
		BayesianFactor Qlws;
		BayesianFactor Qve;

		evidence = new Int2IntOpenHashMap();
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("P(Rain) =                                     " + Qlws);
		System.out.println("P(Rain) =                                     " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap(new int[]{3, 4}, new int[]{0, 1});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LWS: P(Rain|Wet Grass = false, Slippery = true) =  " + Qlws);
		System.out.println("VE:  P(Rain|Wet Grass = false, Slippery = true) =  " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap(new int[]{3, 4}, new int[]{0, 0});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LWS: P(Rain|Wet Grass = false, Slippery = false) = " + Qlws);
		System.out.println("VE:  P(Rain|Wet Grass = false, Slippery = false) = " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap(new int[]{0}, new int[]{1});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LWS: P(Rain|Winter = true) =                       " + Qlws);
		System.out.println("VE:  P(Rain|Winter = true) =                       " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new Int2IntOpenHashMap(new int[]{0}, new int[]{0});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("LWS: P(Rain|Winter = false) =                      " + Qlws);
		System.out.println("VE:  P(Rain|Winter = false) =                      " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);
	}

	@Test
	public void vsVariableElimination3() {
		RandomUtil.setRandomSeed(42);
		final Random random = RandomUtil.getRandom();

		final int e = 5;
		final int m = 1000;
		final int n = 10;
		final int p = 3;

		final BayesianNetwork model = BayesianNetworkContainer.random(n, p).network;

		// TODO: this test has an issue with variable elimination and empty nodes

		MinFillOrdering ordering = new MinFillOrdering();
		int[] seq = ordering.apply(model);

		double avg = 0.0;

		for (int q = 0; q < m; q++) {
			Int2IntMap evidence = new Int2IntOpenHashMap(new int[]{3}, new int[]{0});

			int query = random.nextInt(n);

			for (int i = 0; i < e; i++) {
				int k = random.nextInt(n);
				if (k != query) {
					int v = random.nextInt(2);
					evidence.put(k, v);
				}
			}

			LikelihoodWeightingSampling lws = new LikelihoodWeightingSampling();
			BayesianFactor resLWS = lws.query(model, evidence, query);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			BayesianFactor resVE = ve.query(model, evidence, query);

			double distance = resLWS.getData()[0] - resVE.getData()[0];

			avg += distance;

			System.out.println("LWS:  " + resLWS);
			System.out.println("VE:   " + resVE);
			System.out.println("dist: " + distance);
		}

		avg /= m;

		System.out.println("Average distance; " + avg);
		Assertions.assertTrue(Math.abs(avg) < .07);
	}
}