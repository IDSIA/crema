package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:18
 */
public interface IBayesianFactor extends OperableFactor<IBayesianFactor>, SeparatelySpecified<IBayesianFactor> {

	@Override
	IBayesianFactor copy();

	void sortDomain();

	double[] getData();

	IBayesianFactor addition(IBayesianFactor factor);
}
