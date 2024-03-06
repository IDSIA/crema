package ch.idsia.crema.inference.fe;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 14:16
 */
public class FactorEliminationUtils {

	/**
	 * Given a {@link BayesianFactor} Fr, marginalize all the variables of this factor that are not equals to the
	 * query variable Q.
	 *
	 * @param phi the target factor
	 * @param Q   the query variable
	 * @return a factor over the query variable
	 */
	public static BayesianFactor project(BayesianFactor phi, int... Q) {

		IntSet variables = new IntOpenHashSet(phi.getDomain().getVariables());
		for (int q : Q) {
			variables.remove(q);
		}

		for (int v : variables)
			phi = phi.marginalize(v);

		return phi;
	}

}
