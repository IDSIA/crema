package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Test;

import java.util.Arrays;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:05
 */
public class LikelihoodWeightingSamplingTest extends StochasticSamplingTest {

	@Test
	public void vsVariableElimination() throws InterruptedException {
		TIntIntMap evidence = new TIntIntHashMap(new int[]{3}, new int[]{0});

		for (int query = 0; query < 5; query++) {
			LikelihoodWeightingSampling lws = new LikelihoodWeightingSampling();
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
		LikelihoodWeightingSampling lws = new LikelihoodWeightingSampling();

		TIntIntMap evidence = new TIntIntHashMap();
		System.out.println("P(Rain) =                                     " + lws.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1});
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + lws.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0});
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + lws.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{1});
		System.out.println("P(Rain|Winter = true) =                       " + lws.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{0});
		System.out.println("P(Rain|Winter = false) =                      " + lws.query(model, evidence, 2));
	}
}