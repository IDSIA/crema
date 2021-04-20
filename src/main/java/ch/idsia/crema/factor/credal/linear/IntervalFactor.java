package ch.idsia.crema.factor.credal.linear;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:20
 */
public interface IntervalFactor extends SeparateLinearFactor<IntervalFactor> {

	@Override
	IntervalFactor copy();

	@Override
	Strides getDomain();

	@Override
	Strides getDataDomain();

	@Override
	Strides getSeparatingDomain();

	@Override
	IntervalFactor filter(int variable, int state);

	double[] getUpper(int... states);

	double[] getLower(int... states);

	double[] getUpperAt(int group_offset);

	double[] getLowerAt(int group_offset);

	IntervalFactor merge(IntervalFactor factor);

	boolean updateReachability();

	boolean isInside(BayesianFactor f);

	/**
	 * Merges the bounds with other interval factors
	 *
	 * @param factors
	 * @return
	 */
	default IntervalFactor merge(IntervalFactor... factors) {
		if (factors.length == 1)
			return merge(factors[0]);

		IntervalFactor out = this;

		for (IntervalFactor f : factors)
			out = out.merge(f);

		return out;
	}

	static IntervalFactor mergeBounds(IntervalFactor... factors) {
		return factors[0].merge(Arrays.copyOfRange(factors, 1, factors.length));
	}

}
