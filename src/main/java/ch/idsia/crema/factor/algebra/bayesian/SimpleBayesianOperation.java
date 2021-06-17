package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 18:57
 * <p>
 * Generic implementation that uses the methods of the {@link BayesianFactor} interface to perform operations <u>not</u>
 * in log-space.
 *
 * @param <F> type of the input factors
 */
public class SimpleBayesianOperation<F extends BayesianFactor> implements BayesianOperation<F> {

	@Override
	public double add(F f1, int idx1, F f2, int idx2) {
		return f1.getValueAt(idx1) + f2.getValueAt(idx2);
	}

	@Override
	public double combine(F f1, int idx1, F f2, int idx2) {
		return f1.getValueAt(idx1) * f2.getValueAt(idx2);
	}

	@Override
	public double divide(F f1, int idx1, F f2, int idx2) {
		return f1.getValueAt(idx1) / f2.getValueAt(idx2);
	}

}
