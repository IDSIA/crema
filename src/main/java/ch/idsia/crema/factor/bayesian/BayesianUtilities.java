package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

import java.util.Collection;
import java.util.stream.DoubleStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.08.2021 17:58
 */
public class BayesianUtilities {

	/**
	 * Compute the KLDivergence between two {@link BayesianFactor}s.
	 * @param base
	 * @param approx
	 * @return
	 */
	public static double KLDivergence(BayesianFactor base, BayesianFactor approx) {
		IndexIterator it = approx.getDomain().getReorderedIterator(base.getDomain().getVariables());
		double kl = 0;
		for (int i = 0; i < base.getData().length; i++) {
			int j = it.next();
			double p = base.getValueAt(i);
			double q = approx.getValueAt(j);

			kl += p * (Math.log(p) - Math.log(q));
		}
		return kl;
	}

	// TODO: what to do with this method?
	public boolean isDeterministic(BayesianFactor f, int... given) {
		if (!DoubleStream.of(f.getData()).allMatch(x -> x == 0.0 || x == 1.0))
			return false;

		int[] left = ArraysUtil.difference(f.getDomain().getVariables(), given);

		for (int v : left) {
			f = f.marginalize(v);
		}

		// TODO: _all_ match?
		return DoubleStream.of(f.getData()).allMatch(x -> x == 1.0);
	}

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param factors
	 * @return
	 */
	public static BayesianDefaultFactor combineAll(BayesianDefaultFactor... factors) {
		if (factors.length < 1)
			throw new IllegalArgumentException("wrong number of factors");
		else if (factors.length == 1)
			return factors[0].copy();

		BayesianDefaultFactor out = factors[0];
		for (int i = 1; i < factors.length; i++) {
			out = out.combine(factors[i]);
		}
		return out;
	}

	public static BayesianDefaultFactor combineAll(Collection<BayesianDefaultFactor> factors) {
		return combineAll(factors.toArray(BayesianDefaultFactor[]::new));
	}
}
