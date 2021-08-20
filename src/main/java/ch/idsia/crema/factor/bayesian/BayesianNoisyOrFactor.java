package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;

import java.util.function.Function;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.08.2021 15:33
 */
public class BayesianNoisyOrFactor extends BayesianFunctionFactor {

	public BayesianNoisyOrFactor(Strides domain, Function<Integer, Double> f) {
		super(domain, f); // TODO: build the correct function
	}

	@Override
	public BayesianFactor copy() {
		return null;
	}

	// TODO: this should be a BayesianFunctionFactor (or a BayesianOrFactor?)

}
