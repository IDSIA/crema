package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


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
			Collection<BayesianFactor> collLWS = lws.apply(model, query, evidence);

			BayesianFactor resLWS = new ArrayList<>(collLWS).get(0);

			MinFillOrdering ordering = new MinFillOrdering();
			int[] seq = ordering.apply(model);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			BayesianFactor resVE = ve.apply(model, query, evidence);

			double distance0 = resLWS.getData()[0] - resVE.getData()[0];

			System.out.println("LWS: " + resLWS + " " + Arrays.toString(resLWS.getData()));
			System.out.println("VE:  " + resVE + " " + Arrays.toString(resVE.getData()));
			System.out.println(distance0 + " ");
		}
	}

	@Test
	public void run() {
		LikelihoodWeightingSampling lws = new LikelihoodWeightingSampling();

		lws.setModel(model);
		lws.setEvidence(new TIntIntHashMap());
		System.out.println("P(Rain) =                                     " + factorsToString(lws.run(2)));

		lws.setEvidence(new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1}));
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + factorsToString(lws.run(2)));

		lws.setEvidence(new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0}));
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + factorsToString(lws.run(2)));

		lws.setEvidence(new TIntIntHashMap(new int[]{0}, new int[]{1}));
		System.out.println("P(Rain|Winter = true) =                       " + factorsToString(lws.run(2)));

		lws.setEvidence(new TIntIntHashMap(new int[]{0}, new int[]{0}));
		System.out.println("P(Rain|Winter = false) =                      " + factorsToString(lws.run(2)));
	}
}