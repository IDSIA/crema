package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 15:44
 */
public class LWSvsVE {

	private BayesianNetwork model;

	private final Random random = new Random(42);

	private static final int e = 5;
	private static final int m = 1000;
	private static final int n = 10;
	private static final int p = 3;

	@Before
	public void setUp() {
		BayesianNetworkContainer BN = BayesianNetworkContainer.random(42, n, p);

		model = BN.network;
	}

	@Test
	public void vsVariableElimination() throws InterruptedException {

		// TODO: this test has an issue with variable elimination and empty nodes

		MinFillOrdering ordering = new MinFillOrdering();
		int[] seq = ordering.apply(model);

		double avg = 0.0;

		for (int q = 0; q < m; q++) {
			TIntIntMap evidence = new TIntIntHashMap(new int[]{3}, new int[]{0});

			int query = random.nextInt(n);

			for (int i = 0; i < e; i++) {
				int k = random.nextInt(n);
				if (k != query) {
					int v = random.nextInt(2);
					evidence.put(k, v);
				}
			}

			LikelihoodWeightingSampling lws = new LikelihoodWeightingSampling();
			Collection<BayesianFactor> collLWS = lws.apply(model, query, evidence);

			BayesianFactor resLWS = new ArrayList<>(collLWS).get(0);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			BayesianFactor resVE = ve.apply(model, query, evidence);

			double distance = resLWS.getData()[0] - resVE.getData()[0];

			avg += distance;

			System.out.println("LWS: " + resLWS + " " + Arrays.toString(resLWS.getData()));
			System.out.println("VE:  " + resVE + " " + Arrays.toString(resVE.getData()));
			System.out.println(distance + " ");
		}

		avg /= m;

		System.out.println("Average distance; " + avg);
	}

}
