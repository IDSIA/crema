package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.Observe;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 13:33
 */
public class LikelihoodWeightingSampling extends StochasticSampling {

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
	public Collection<BayesianFactor> run(GraphicalModel<BayesianFactor> original, Int2IntMap evidence, int... query) {
		final GraphicalModel<BayesianFactor> model = preprocess(original, evidence, query);

		if (!preprocess) {
			// this is mandatory
			final Observe<BayesianFactor> co = new Observe<>();
			co.executeInPlace(model, evidence);
		}

		// P[x] <- 0 for each value x of variable X in network N {estimate for Pr(x,e)}}
		final Int2ObjectMap<double[]> Px = new Int2ObjectOpenHashMap<>();

		for (int variable : model.getVariables()) {
			int states = model.getSize(variable);
			Px.put(variable, new double[states]);
		}

		// for each round of simulations
		for (int it = 0; it < iterations; it++) {
			final Int2IntMap x = simulateBN(model, evidence);
			final Int2DoubleMap likelihoods = new Int2DoubleOpenHashMap();

			// collect likelihood of evidence
			for (int key : evidence.keySet()) {
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

			final double L = likelihoods.values().doubleStream()
//					.mapToDouble(likelihoods::get)
					.reduce((a, b) -> a * b)
					.orElse(1.0);

			// update the counts
			for (int key : x.keySet()) {
				final int state = x.get(key);
				Px.get(key)[state] += L;
			}
		}

		return Arrays.stream(query)
				.mapToObj(q -> new BayesianDefaultFactor(model.getDomain(q), Px.get(q)))
				.collect(Collectors.toList());
	}

}
