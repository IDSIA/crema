package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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

	private TIntIntMap evidence;

	private Random random;
	private long seed = 42L;

	private long iterations = 1000;

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
	 * Fix some evidence. The provided argument is a map of variable - state associations.
	 *
	 * @param evidence the map of observations
	 */
	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	/**
	 * Default value is 1000.
	 *
	 * @param iterations number of iterations to do during the sampling
	 */
	public void setIterations(long iterations) {
		this.iterations = iterations;
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
			// check if the state has evidence, else sample it
			int state;
			if (evidence == null || !evidence.containsKey(root)) {
				state = sample(factors[root]);
			} else {
				state = evidence.get(root);
			}

			map.put(root, state);

			int[] children = model.getChildren(root);
			nodes.addAll(children);
		}

		// sample child nodes
		do {
			TIntSet slack = new TIntHashSet();

			for (int node : nodes.toArray()) {
				int state;
				// check for evidence in this child node
				if (evidence == null || !evidence.containsKey(node)) {
					int[] parents = model.getParents(node);

					// filter out parents state
					BayesianFactor factor = factors[node];
					for (int parent : parents) {
						factor = factor.filter(parent, map.get(parent));
					}

					state = sample(factor);
				} else {
					state = evidence.get(node);
				}
				map.put(node, state);

				int[] children = model.getChildren(node);
				slack.addAll(children);
			}

			nodes = slack;
		} while (!nodes.isEmpty());

		return map;
	}

	/**
	 * Algorithm 45 from "Modeling and Reasoning with BN", Dawiche, p.385
	 * <p>
	 * Use Monte Carlo simulation to estimate the expectation of the direct sampling function.
	 *
	 * @param query variable to query
	 * @return the distribution of probability on the query
	 */
	public double[] directSampling(int query) {

		// TODO: extends to multiple query variables

		TIntObjectMap<double[]> distributions = new TIntObjectHashMap<>();

		for (int variable : model.getVariables()) {
			int states = model.getDomain(variable).getCombinations();

			distributions.put(variable, new double[states]);
		}

		for (int i = 0; i < iterations; i++) {
			TIntIntMap x = simulateBN();

			for (int variable : x.keys()) {
				distributions.get(variable)[x.get(variable)] += 1. / iterations;
			}
		}

		return distributions.get(query);
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

	/**
	 * Algorithm 46 from "Modeling and Reasoning with BN", Dawiche, p.380
	 */
	public void likelihoodWeighting(int query) {
		if (evidence == null)
			throw new IllegalArgumentException("Setting the evidence is mandatory!");

		SparseModel<BayesianFactor> N = model.copy();

		// remove connections between node with evidence and parents
		for (int node : evidence.keys()) {
			int[] parents = N.getParents(node);
			for (int parent : parents) {
				N.removeParent(node, parent);
			}
		}

		for (int i = 0; i < iterations; i++) {

			TIntIntMap x = simulateBN();
			// TODO

		}
	}
}
