package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:05
 */
public class LikelihoodWeightingSamplingTest {

	BayesianNetwork model;
	LikelihoodWeightingSampling lws;

	@BeforeEach
	public void setUp() {
		BayesianNetworkContainer BN = BayesianNetworkContainer.mix5Variables();

		model = BN.network;
		lws = new LikelihoodWeightingSampling();
		lws.setPreprocess(false);

		RandomUtil.setRandomSeed(0);
	}

	@Test
	public void vsVariableElimination() {
		TIntIntMap evidence = new TIntIntHashMap(new int[]{3}, new int[]{0});

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
	public void run() {
		VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(new int[]{4, 3, 1, 0, 2});
		lws.setIterations(100000);

		TIntIntMap evidence;
		BayesianFactor Qlws;
		BayesianFactor Qve;

		evidence = new TIntIntHashMap();
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("P(Rain) =                                     " + Qlws);
		System.out.println("P(Rain) =                                     " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + Qlws);
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + Qlws);
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{1});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("P(Rain|Winter = true) =                       " + Qlws);
		System.out.println("P(Rain|Winter = true) =                       " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{0});
		Qlws = lws.query(model, evidence, 2);
		Qve = ve.query(model, evidence, 2);
		System.out.println("P(Rain|Winter = false) =                      " + Qlws);
		System.out.println("P(Rain|Winter = false) =                      " + Qve);

		assertEquals(Qlws.getValue(0), Qve.getValue(0), 0.01);
	}
}