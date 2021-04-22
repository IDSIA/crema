package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 21:21
 */
public class SimpleBayesianFilter implements BayesianCollector {

	private final int offset;

	public SimpleBayesianFilter(int stride, int state) {
		offset = stride * state;
	}

	@Override
	public double collect(BayesianFactor factor, int source) {
		return factor.getValueAt(source + offset);
	}

}
