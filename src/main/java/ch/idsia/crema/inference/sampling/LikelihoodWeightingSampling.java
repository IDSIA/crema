package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.Updating;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 13:33
 */
public class LikelihoodWeightingSampling extends StochasticSampling implements Updating<BayesianFactor, BayesianFactor> {

	/**
	 * Algorithm 44 from "Modeling and Reasoning with BN", Dawiche, p.380
	 *
	 * @return a map with the computed sampled states over all the variables.
	 */
	private TIntIntMap simulateBN() {

		int[] roots = model.getRoots();

		TIntIntMap map = new TIntIntHashMap();
		TIntSet nodes = new TIntHashSet();

		// sample root nodes
		for (int root : roots) {
			// check if the state has evidence, else sample it
			int state;
			if (evidence == null || !evidence.containsKey(root)) {
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
			TIntSet slack = new TIntHashSet();

			for (int node : nodes.toArray()) {
				int state;
				// check for evidence in this child node
				if (evidence == null || !evidence.containsKey(node)) {
					int[] parents = model.getParents(node);

					// filter out parents state
					BayesianFactor factor = model.getFactor(node);
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
	 * Algorithm 46 from "Modeling and Reasoning with BN", Dawiche, p.380
	 */
	public Collection<BayesianFactor> run(int... query) {
		if (evidence == null)
			throw new IllegalArgumentException("Setting the evidence is mandatory!");

		long n = iterations;

		GraphicalModel<BayesianFactor> N = model.copy();

		// remove connections between node with evidence and parents
		for (int node : evidence.keys()) {
			int[] parents = N.getParents(node);
			for (int parent : parents) {
				N.removeParent(node, parent);
			}
		}

		double P = 0; // P <- 0 {estimate for Pr(e)}

		// P[x] <- 0 for each value x of variable X in network N {estimate for Pr(x,e)}}
		TIntObjectMap<double[]> Px = new TIntObjectHashMap<>();

		for (int variable : model.getVariables()) {
			int states = model.getDomain(variable).getCombinations();
			Px.put(variable, new double[states]);
		}

		for (int i = 0; i < n; i++) {
			TIntIntMap x = simulateBN();

			// product of all network parameters theta_e|u where E in _E_ and eu ~ x
			double W = 1;

			for (int key : evidence.keys()) {
				BayesianFactor factor = model.getFactor(key);
				for (int parent : model.getParents(key)) {
					factor = factor.filter(parent, x.get(parent));
				}

				W *= factor.getValue(evidence.get(key));
			}

			P += W;

			for (int key : x.keys()) {
				int state = x.get(key);

				BayesianFactor factor = model.getFactor(key);
				for (int parent : model.getParents(key)) {
					factor = factor.filter(parent, x.get(parent));
				}

				Px.get(key)[state] += W;
			}
		}

		for (int var : Px.keys()) {
			double[] d = Px.get(var);
			for (int i = 0; i < d.length; i++) {
				d[i] /= P;
			}
		}

		List<BayesianFactor> factors = new ArrayList<>();

		for (int q : query) {
			factors.add(new BayesianFactor(model.getDomain(q), Px.get(q), false));
		}

		return factors;
	}

	@Override
	public Collection<BayesianFactor> apply(GraphicalModel<BayesianFactor> model, int[] query) {
		setModel(model);
		setEvidence(new TIntIntHashMap());

		return run(query);
	}

	@Override
	public Collection<BayesianFactor> apply(GraphicalModel<BayesianFactor> model, int[] query, TIntIntMap observations) {
		setModel(model);
		setEvidence(observations);

		return run(query);
	}
}
