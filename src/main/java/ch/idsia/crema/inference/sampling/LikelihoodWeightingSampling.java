package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.preprocess.CutObserved;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 13:33
 */
public class LikelihoodWeightingSampling extends StochasticSampling implements InferenceJoined<BayesianNetwork, BayesianFactor> {

	public LikelihoodWeightingSampling() {
	}

	public LikelihoodWeightingSampling(Boolean preprocess) {
		super(preprocess);
	}

	public LikelihoodWeightingSampling(long iterations) {
		super(iterations);
	}

	public LikelihoodWeightingSampling(long iterations, Boolean preprocess) {
		super(iterations, preprocess);
	}

	/**
	 * Algorithm 46 from "Modeling and Reasoning with BN", Dawiche, p.380
	 */
	@Override
	public Collection<BayesianFactor> run(BayesianNetwork original, TIntIntMap evidence, int... query) {
		final BayesianNetwork model = preprocess(original, evidence, query);

		if (!preprocess) {
			// this is mandatory
			final CutObserved<BayesianFactor> co = new CutObserved<>();
			co.executeInPlace(model, evidence);
		}

		// P[x] <- 0 for each value x of variable X in network N {estimate for Pr(x,e)}}
		final TIntObjectMap<double[]> Px = new TIntObjectHashMap<>();

		for (int variable : model.getVariables()) {
			int states = model.getSize(variable);
			Px.put(variable, new double[states]);
		}

		// for each round of simulations
		for (int it = 0; it < iterations; it++) {
			final TIntIntMap x = simulateBN(model, evidence);
			final TIntDoubleMap likelihoods = new TIntDoubleHashMap();

			// collect likelihood of evidence
			for (int key : evidence.keys()) {
				if (model.getSize(key) == 0)
					continue;

				final int state = evidence.get(key);
				final int[] parents = model.getParents(key);
				BayesianFactor factor = model.getFactor(key);

				if (parents.length == 0) {
					likelihoods.put(key, factor.getValue(state));
				} else {
					for (int p : parents)
						factor = factor.filter(p, x.get(p));

					likelihoods.put(key, factor.getValue(x.get(key)));
				}
			}

			final double L = Arrays.stream(likelihoods.keys())
					.mapToDouble(likelihoods::get)
					.reduce((a, b) -> a * b)
					.orElse(1.0);

			// update the counts
			for (int key : x.keys()) {
				final int state = x.get(key);
				Px.get(key)[state] += L;
			}
		}

		return Arrays.stream(query)
				.mapToObj(q -> new BayesianFactor(model.getDomain(q), Px.get(q)))
				.collect(Collectors.toList());
	}

	/**
	 * @deprecated use method {@link #query(BayesianNetwork, TIntIntMap, int)}
	 */
	@Deprecated
	public Collection<BayesianFactor> apply(BayesianNetwork model, int[] query) {
		return run(model, new TIntIntHashMap(), query);
	}

	/**
	 * @deprecated use method {@link #query(BayesianNetwork, TIntIntMap, int[])}
	 */
	@Deprecated
	public Collection<BayesianFactor> apply(BayesianNetwork model, int[] query, TIntIntMap observations) {
		return run(model, observations, query);
	}

}
