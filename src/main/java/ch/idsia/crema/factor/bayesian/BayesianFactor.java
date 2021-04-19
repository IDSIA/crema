package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
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

	@Override
	BayesianFactor filter(int variable, int state);

	@Override
	BayesianFactor combine(BayesianFactor other);

	@Override
	BayesianFactor marginalize(int variable);

	@Override
	BayesianFactor divide(BayesianFactor factor);

	@Override
	Strides getSeparatingDomain();

	@Override
	Strides getDomain();

	@Override
	Strides getDataDomain();

	double getValue(int... states);

	double getValueAt(int index);

	BayesianFactor replace(double value, double replacement);

	BayesianFactor replaceNaN(double replacement);

	// TODO: these methods below need more consideration on what to do with them and where to put them

	void sortDomain();

	BayesianFactor addition(BayesianFactor factor);

	double logProb(TIntIntMap[] data, int leftVar);

	void replaceInplace(double value, double replacement);

}
