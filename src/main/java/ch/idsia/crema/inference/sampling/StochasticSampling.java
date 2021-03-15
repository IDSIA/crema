package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public abstract class StochasticSampling implements InferenceJoined<BayesianNetwork, BayesianFactor> {

	protected BayesianNetwork model;

	protected TIntIntMap evidence;

	protected long iterations = 100;

	protected Boolean preprocess = true;

	/**
	 * @param preprocess true to activate pre-processing, default is true.
	 * @return the same object that can be chained during the configuration
	 */
	public StochasticSampling setPreprocess(Boolean preprocess) {
		this.preprocess = preprocess;
		return this;
	}

	protected BayesianNetwork preprocess(BayesianNetwork original, TIntIntMap evidence, int... query) {
		BayesianNetwork model = original;
		if (preprocess) {
			model = new BayesianNetwork(original.copy());
			final CutObserved<BayesianFactor> co = new CutObserved<>();
			final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();

			co.executeInPlace(model, evidence);
			rb.executeInPlace(model, evidence, query);
		}

		return model;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setModel(BayesianNetwork model) {
		this.model = (BayesianNetwork) model.copy();
	}

	/**
	 * Default value is 100.
	 *
	 * @param iterations number of iterations to do during the sampling
	 */
	public StochasticSampling setIterations(long iterations) {
		this.iterations = iterations;
		return this;
	}

	/**
	 * Algorithm 44 from "Modeling and Reasoning with BN", Dawiche, p.380
	 *
	 * @return a map with the computed sampled states over all the variables.
	 */
	protected TIntIntMap simulateBN(BayesianNetwork model, TIntIntMap evidence) {
		final TIntIntMap map = new TIntIntHashMap();
		TIntSet nodes = new TIntHashSet();

		// sample root nodes
		for (int root : model.getRoots()) {
			// check if the state has evidence, else sample it
			int state;
			if (!evidence.containsKey(root)) {
				state = sample(model.getFactor(root));
			} else {
				state = evidence.get(root);
			}

			map.put(root, state);

			int[] children = model.getChildren(root);
			nodes.addAll(children);
		}

		// sample child nodes
		do {
			final TIntSet slack = new TIntHashSet();

			for (int node : nodes.toArray()) {
				// check if all parents have already been sampled...
				final int[] parents = model.getParents(node);

				boolean all = true;
				for (int p : parents) {
					if (!map.containsKey(p)) {
						all = false;
						break;
					}
				}

				// ...if not, postpone the node to the next pass
				if (!all) {
					slack.add(node);
					continue;
				}

				// check for evidence in this child node
				if (evidence.containsKey(node)) {
					// with evidence the state is fixed
					map.put(node, evidence.get(node));
				} else {
					// check for parent state in this child node
					final TIntIntMap obs = new TIntIntHashMap();
					for (int p : parents)
						obs.put(p, map.get(p));

					map.put(node, sample(model.getFactor(node), obs));
				}

				final int[] children = model.getChildren(node);
				slack.addAll(children);
			}

			nodes = slack;
		} while (!nodes.isEmpty());

		return map;
	}

	protected abstract Collection<BayesianFactor> run(BayesianNetwork model, TIntIntMap evidence, int... query);

	/**
	 * Sample the distribution of a {@link BayesianFactor} with not parent nodes.
	 * Note that this factor distribution is over a single variable!
	 *
	 * @param factor factor to sample
	 * @return the index of sampled state
	 */
	// TODO: this should not be in an inference package
	protected int sample(BayesianFactor factor) {
		double[] data = factor.getData();
		double p = RandomUtil.getRandom().nextDouble();

		double sum = 0.0;

		int i = 0;
		for (; i < data.length; i++) {
			sum += data[i];
			if (sum > p)
				break;
		}

		return i;
	}

	/**
	 * Sample the distribution of a {@link BayesianFactor} with a specific set of parent observations.
	 *
	 * @param factor factor to sample
	 * @param obs    map of the states of the parent nodes
	 * @return the index of sampled state
	 */
	protected int sample(BayesianFactor factor, TIntIntMap obs) {
		final BayesianFactor f = factor.filter(obs);
		return sample(f);
	}

	@Override
	public BayesianFactor query(BayesianNetwork model, int query) {
		return query(model, new TIntIntHashMap(), new int[]{query});
	}

	@Override
	public BayesianFactor query(BayesianNetwork model, TIntIntMap evidence, int... query) {
		return run(model, evidence, query).stream()
				.reduce(BayesianFactor::combine)
				.orElseThrow(() -> new IllegalStateException("Could not produce a joint probability"))
				.normalize();
	}

	@Override
	public BayesianFactor query(BayesianNetwork model, int... queries) {
		return query(model, new TIntIntHashMap(), queries);
	}

	@Override
	public BayesianFactor query(BayesianNetwork model, TIntIntMap evidence, int query) {
		return query(model, evidence, new int[]{query});
	}

}
