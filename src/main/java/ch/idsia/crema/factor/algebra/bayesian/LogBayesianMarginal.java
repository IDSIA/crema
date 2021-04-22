package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 21:23
 */
public class LogBayesianMarginal implements BayesianCollector {

	private final int[] offsets;
	private final int size;

	public LogBayesianMarginal(int size, int stride) {
		this.size = size;
		offsets = new int[size];
		for (int i = 0; i < size; ++i) {
			offsets[i] = i * stride;
		}
	}

	@Override
	public final double collect(BayesianFactor factor, final int source) {
		double value = 0;
		for (int i = 0; i < size; ++i) {
			value += factor.getLogValueAt(source + offsets[i]);
		}
		return value;
	}
}
