package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:18
 */
public interface BayesianFactor extends OperableFactor<BayesianFactor>, SeparatelySpecified<BayesianFactor> {

	@Override
	BayesianFactor copy();

	void sortDomain();

	double[] getData();

	BayesianFactor addition(BayesianFactor factor);

	double logProb(TIntIntMap[] data, int leftVar);

	double getValue(int... states);

	double getValueAt(int index);

	void replaceInLine(double value, double replacement);
}
