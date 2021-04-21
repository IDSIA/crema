package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 18:57
 */
public class SimpleBayesianOperation implements BayesianOperation {

	@Override
	public <F extends BayesianFactor> double add(F f1, int idx1, F f2, int idx2) {
		return f1.getValueAt(idx1) + f2.getValueAt(idx2);
	}

	@Override
	public <F extends BayesianFactor> double combine(F f1, int idx1, F f2, int idx2) {
		return f1.getValueAt(idx1) * f2.getValueAt(idx2);
	}

	@Override
	public <F extends BayesianFactor> double divide(F f1, int idx1, F f2, int idx2) {
		return f1.getValueAt(idx1) / f2.getValueAt(idx2);
	}

}
