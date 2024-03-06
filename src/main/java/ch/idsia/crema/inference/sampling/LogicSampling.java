package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 13:32
 */
public class LogicSampling extends StochasticSampling {

	public LogicSampling() {
	}

	public LogicSampling(Boolean preprocess) {
		super(preprocess);
	}

	public LogicSampling(long iterations) {
		super(iterations);
	}

	public LogicSampling(long iterations, Boolean preprocess) {
		super(iterations, preprocess);
	}

	/**
	 * Algorithm 45 from "Modeling and Reasoning with BN", Dawiche, p.385
	 * <p>
	 * Use Monte Carlo simulation to estimate the expectation of the direct sampling function.
	 *
	 * @param original model to use for inference
	 * @param query    variable to query
	 * @return the distribution of probability on the query
	 */
	@Override
	public Collection<BayesianFactor> run(GraphicalModel<BayesianFactor> original, Int2IntMap evidence, int... query) {
		final GraphicalModel<BayesianFactor> model = preprocess(original, evidence, query);
		final Int2ObjectMap<double[]> Px = new Int2ObjectOpenHashMap<>();

		for (int variable : model.getVariables()) {
			final int states = model.getSize(variable);
			Px.put(variable, new double[states]);
		}

		for (int it = 0; it < iterations; it++) {
			final Int2IntMap x = simulateBN(model, evidence);

			for (int variable : x.keySet()) {
				final int state = x.get(variable);
				Px.get(variable)[state] += 1.;
			}
		}

		return Arrays.stream(query)
				.mapToObj(q -> new BayesianDefaultFactor(model.getDomain(q), Px.get(q)))
				.collect(Collectors.toList());
	}

}
