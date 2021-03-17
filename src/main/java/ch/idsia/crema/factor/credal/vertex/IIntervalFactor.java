package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 13:20
 */
public interface IIntervalFactor extends SeparateLinearFactor<IIntervalFactor> {

	@Override
	IIntervalFactor copy();

	double[] getUpper(int... states);

	double[] getLower(int... states);

	double[] getUpperAt(int group_offset);

	double[] getLowerAt(int group_offset);

	double[][] getDataUpper();

	double[][] getDataLower();

	IIntervalFactor merge(IIntervalFactor factor);

	/**
	 * Merges the bounds with other interval factors
	 *
	 * @param factors
	 * @return
	 */
	default IIntervalFactor merge(IIntervalFactor... factors) {
		if (factors.length == 1)
			return merge(factors[0]);

		IIntervalFactor out = this;

		for (IIntervalFactor f : factors)
			out = out.merge(f);

		return out;
	}

	static IIntervalFactor mergeBounds(IIntervalFactor... factors) {
		return factors[0].merge(Arrays.copyOfRange(factors, 1, factors.length));
	}
}
