package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
	public Collection<BayesianFactor> run(DAGModel<BayesianFactor> original, TIntIntMap evidence, int... query) {
		final DAGModel<BayesianFactor> model = preprocess(original, evidence, query);
		final TIntObjectMap<double[]> Px = new TIntObjectHashMap<>();

		for (int variable : model.getVariables()) {
			final int states = model.getSize(variable);
			Px.put(variable, new double[states]);
		}

		for (int it = 0; it < iterations; it++) {
			final TIntIntMap x = simulateBN(model, evidence);

			for (int variable : x.keys()) {
				final int state = x.get(variable);
				Px.get(variable)[state] += 1.;
			}
		}

		return Arrays.stream(query)
				.mapToObj(q -> new BayesianFactor(model.getDomain(q), Px.get(q)))
				.collect(Collectors.toList());
	}

	/**
	 * @deprecated use method {@link #query(DAGModel, int[])}
	 */
	@Deprecated
	public Collection<BayesianFactor> apply(DAGModel<BayesianFactor> model, int[] query) {
		return run(model, new TIntIntHashMap(), query);
	}

}
