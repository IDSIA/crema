package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.algebra.OperationUtils;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 18:57
 */
public class LogBayesianOperation implements BayesianOperation {

	@Override
	public <F extends BayesianFactor> double add(F f1, int idx1, F f2, int idx2) {
		return OperationUtils.logSum(f1.getLogValueAt(idx1), f2.getLogValueAt(idx2));
	}

	@Override
	public <F extends BayesianFactor> double combine(F f1, int idx1, F f2, int idx2) {
		return f1.getLogValueAt(idx1) + f2.getLogValueAt(idx2);
	}

	@Override
	public <F extends BayesianFactor> double divide(F f1, int idx1, F f2, int idx2) {
		return f1.getLogValueAt(idx1) - f2.getLogValueAt(idx2);
	}

}
