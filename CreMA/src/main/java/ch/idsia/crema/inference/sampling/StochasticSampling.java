package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public class StochasticSampling {

	private BayesianFactor[] factors;
	private SparseModel<BayesianFactor> model;

	private Random random;
	private long seed = 42L;

	/**
	 * Creates a new object based on the given model and array of {@link BayesianFactor}s.
	 *
	 * @param model   network model representation
	 * @param factors array of factors associated to each variable in the model (index equal variable id)
	 */
	public StochasticSampling(SparseModel<BayesianFactor> model, BayesianFactor[] factors) {
		this.model = model;
		this.factors = factors;
		this.random = new Random(seed);
	}

	/**
	 * Set random seed (this re-initialize the random).
	 *
	 * @param seed new seed.
	 */
	public void setSeed(long seed) {
		this.seed = seed;
		random = new Random(seed);
	}

	/**
	 * Algorithm 44 from "Modeling and Reasoning with BN", Dawiche, p.380
	 *
	 * @return a map with the computed sampled states over all the variables.
	 */
	public TIntIntMap simulateBN() {

		int[] roots = model.getRoots();

		TIntIntMap map = new TIntIntHashMap();
		TIntSet nodes = new TIntHashSet();

		// sample root nodes
		for (int root : roots) {
			int state = sample(factors[root]);
			map.put(root, state);

			int[] children = model.getChildren(root);
			nodes.addAll(children);
		}

		// sample child nodes
		do {
			TIntSet slack = new TIntHashSet();

			for (int node : nodes.toArray()) {
				int[] parents = model.getParents(node);

				// filter out parents state
				BayesianFactor factor = factors[node];
				for (int parent : parents) {
					factor = factor.filter(parent, map.get(parent));
				}

				int state = sample(factor);
				map.put(node, state);

				int[] children = model.getChildren(node);
				slack.addAll(children);
			}

			nodes = slack;
		} while (!nodes.isEmpty());

		return map;
	}

	/**
	 * Sample the distribution of a factor. Note that this factor distribution is over a single variable!
	 *
	 * @param factor factor to sample
	 * @return the index of sampled state
	 */
	private int sample(BayesianFactor factor) {
		double[] data = factor.getData();
		double p = random.nextDouble();

		double sum = 0.0;

		int i = 0;
		for (; i < data.length; i++) {
			sum += data[i];
			if (sum > p)
				break;
		}

		return i;
	}
}
