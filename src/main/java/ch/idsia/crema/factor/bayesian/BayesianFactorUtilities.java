package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.primitives.Doubles;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    15.04.2021 18:40
 */
public class BayesianFactorUtilities {

	public static BayesianFactor random(Strides left, Strides right) {
		double[][] data = new double[right.getCombinations()][];

		for (int i = 0; i < data.length; i++) {
			data[i] = RandomUtil.sampleNormalized(left.getCombinations());
		}

		return new BayesianDefaultFactor(left.concat(right), Doubles.concat(data));
	}

	public static BayesianFactor replaceZerosInMarginal(BayesianFactor f, double EPS) {
		BayesianFactor out = f.copy();
		out.replace(0, EPS);
		return out.normalize();
	}

}
