package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.Observe;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.RandomUtil;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Collection;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public abstract class StochasticSampling implements InferenceJoined<GraphicalModel<BayesianFactor>, BayesianFactor> {

	protected long iterations = 100;

	protected Boolean preprocess = true;

	public StochasticSampling() {
	}

	public StochasticSampling(Boolean preprocess) {
		this.preprocess = preprocess;
	}

	public StochasticSampling(long iterations) {
		this.iterations = iterations;
	}

	public StochasticSampling(long iterations, Boolean preprocess) {
		this.iterations = iterations;
		this.preprocess = preprocess;
	}

	/**
	 * @param preprocess true to activate pre-processing, default is true.
	 * @return the same object for a chained config
	 */
	public StochasticSampling setPreprocess(Boolean preprocess) {
		this.preprocess = preprocess;
		return this;
	}

	/**
	 * @param iterations number to iteration to perform, default value is 100.
	 * @return the same object for a chained config
	 */
	public StochasticSampling setIterations(long iterations) {
		this.iterations = iterations;
		return this;
	}

	protected GraphicalModel<BayesianFactor> preprocess(GraphicalModel<BayesianFactor> original, Int2IntMap evidence, int... query) {
		GraphicalModel<BayesianFactor> model = original;
		if (preprocess) {
			model = original.copy();
			final Observe<BayesianFactor> co = new Observe<>();
			final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();

			co.executeInPlace(model, evidence);
			rb.executeInPlace(model, evidence, query);
		}

		return model;
	}

	/**
	 * Algorithm 44 from "Modeling and Reasoning with BN", Dawiche, p.380
	 *
	 * @return a map with the computed sampled states over all the variables.
	 */
	protected Int2IntMap simulateBN(GraphicalModel<BayesianFactor> model, Int2IntMap evidence) {
		final Int2IntMap map = new Int2IntOpenHashMap();
		IntSet nodes = new IntOpenHashSet();

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

			IntSet children = model.getChildrenSet(root);
			nodes.addAll(children);
		}

		// sample child nodes
		do {
			final IntSet slack = new IntOpenHashSet();

			for (int node : nodes) {
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
					final Int2IntMap obs = new Int2IntOpenHashMap();
					for (int p : parents)
						obs.put(p, map.get(p));

					map.put(node, sample(model.getFactor(node), obs));
				}

				final var children = model.getChildrenSet(node);
				slack.addAll(children);
			}

			nodes = slack;
		} while (!nodes.isEmpty());

		return map;
	}

	protected abstract Collection<BayesianFactor> run(GraphicalModel<BayesianFactor> model, Int2IntMap evidence, int... query);

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
	protected int sample(BayesianFactor factor, Int2IntMap obs) {
		final BayesianFactor f = factor.filter(obs);
		return sample(f);
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, int query) {
		return query(model, Int2IntMaps.EMPTY_MAP, new int[]{query});
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, Int2IntMap evidence, int... query) {
		return run(model, evidence, query).stream()
				.reduce(BayesianFactor::combine)
				.orElseThrow(() -> new IllegalStateException("Could not produce a joint probability"))
				.normalize();
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, int... queries) {
		return query(model, Int2IntMaps.EMPTY_MAP, queries);
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, Int2IntMap evidence, int query) {
		return query(model, evidence, new int[]{query});
	}

}
