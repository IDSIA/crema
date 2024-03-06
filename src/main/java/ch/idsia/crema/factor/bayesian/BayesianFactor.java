package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;
import it.unimi.dsi.fastutil.ints.Int2IntMap;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:18
 */
public interface BayesianFactor extends OperableFactor<BayesianFactor>, SeparatelySpecified<BayesianFactor> {

	@Override
	Strides getDomain();

	@Override
	Strides getDataDomain();

	@Override
	Strides getSeparatingDomain();

	/**
	 * @param states specified in the order of the factor's domain
	 * @return the value associated to the given states
	 */
	double getValue(int... states);

	/**
	 * @param index offset based on the factor's domain
	 * @return the value associated to the given states
	 */
	double getValueAt(int index);

	/**
	 * @param states specified in the order of the factor's domain
	 * @return the value associated to the given states in log format
	 */
	double getLogValue(int... states);

	/**
	 * @param index offset based on the factor's domain
	 * @return the value associated to the given states in log format
	 */
	double getLogValueAt(int index);

	// TODO: this method should only be for internal use
	double[] getData();

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

	// TODO: these methods below need more consideration on what to do with them and where to put them
	
	// TODO: MOVE THIS METHOD TO own data generator
	ObservationBuilder sample();

	// TODO: explain what the method does
	double logProb(Int2IntMap[] data, int leftVar);

}
