package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 15:44
 */
public class LWSvsVE {

	private final Random random = new Random(42);

	private static final int e = 5;
	private static final int m = 1000;
	private static final int n = 10;
	private static final int p = 3;

	@Test
	public void vsVariableElimination() {
		final BayesianNetworkContainer BN = BayesianNetworkContainer.random(42, n, p);
		final BayesianNetwork model = BN.network;

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
			BayesianFactor resLWS = lws.query(model, evidence, query);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			BayesianFactor resVE = ve.query(model, evidence, query);

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
