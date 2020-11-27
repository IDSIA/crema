package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.11.2020 17:01
 */
public class BayesianNetworkSampling {

	/**
	 * @param model model to sample
	 * @param vars
	 * @return
	 */
	public TIntIntMap sample(GraphicalModel<BayesianFactor> model, int... vars) {
		TIntIntMap obs = new TIntIntHashMap();
		for (Integer integer : model.getVariables()) {
			BayesianFactor f = model.getFactor(integer).copy();
			for (int pa : model.getParents(integer)) {
				f = f.filter(pa, obs.get(pa));
			}
			obs.putAll(f.sample());
		}

		if (vars.length == 0)
			vars = model.getVariables();

		for (int v : obs.keys())
			if (!ArraysUtil.contains(v, vars))
				obs.remove(v);

		return obs;
	}

	/**
	 * @param model model to sample
	 * @param N     number of samples
	 * @param vars
	 * @return
	 */
	public TIntIntMap[] samples(GraphicalModel<BayesianFactor> model, int N, int... vars) {
		return IntStream.range(0, N).mapToObj(i -> sample(model, vars)).toArray(TIntIntMap[]::new);
	}

}
