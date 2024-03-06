package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.ArraysUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.11.2020 17:01
 */
// TODO: this should not be under the inference package
// TODO: this class need a test...
public class BayesianNetworkSampling {

	/**
	 * @param model model to use for sampling
	 * @param obs   map of existing observations
	 * @param vars  variables to return
	 * @return a {@link TIntIntMap} of states sampled from the model with the given evidence
	 */
	public Int2IntMap sample(DAGModel<BayesianFactor> model, Int2IntMap obs, int... vars) {
		final Int2IntMap s = new Int2IntOpenHashMap(obs);

		// follow DAG in topological order
		for (int v : model.getNetwork()) {
			if (s.containsKey(v))
				continue;
			BayesianFactor f = model.getFactor(v).copy();
			for (int pa : model.getParents(v)) {
				f = f.filter(pa, s.get(pa));
			}
			s.putAll(f.sample());
		}

		if (vars.length == 0)
			vars = model.getVariables();

		for (int v : s.keySet())
			if (!ArraysUtil.contains(v, vars))
				s.remove(v);

		return s;
	}

	/**
	 * @param model model to use for sampling
	 * @param vars  variables to return
	 * @return a {@link TIntIntMap} of states sampled from the model without prior evidence
	 */
	public Int2IntMap sample(DAGModel<BayesianFactor> model, int... vars) {
		return sample(model, new Int2IntOpenHashMap(), vars);
	}


	/**
	 * @param model model to use for sampling
	 * @param obs   map of existing observations
	 * @param N     number of samples to produce
	 * @param vars  variables to return
	 * @return the specified number of {@link TIntIntMap} of states sampled from the model with the given evidence
	 */
	public Int2IntMap[] samples(DAGModel<BayesianFactor> model, Int2IntMap obs, int N, int... vars) {
		return IntStream.range(0, N).<Int2IntMap>mapToObj(i -> sample(model, obs, vars)).toArray(Int2IntMap[]::new);
	}


	/**
	 * @param model model to use for sampling
	 * @param N     number of samples to produce
	 * @param vars  variables to return
	 * @return the specified number of {@link TIntIntMap} of states sampled from the model without prior evidence
	 */
	public Int2IntMap[] samples(DAGModel<BayesianFactor> model, int N, int... vars) {
		return IntStream.range(0, N).<Int2IntMap>mapToObj(i -> sample(model, vars)).toArray(Int2IntMap[]::new);
	}

}
