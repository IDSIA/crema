package ch.idsia.crema.inference.fe;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

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

		TIntSet variables = new TIntHashSet(phi.getDomain().getVariables());
		for (int q : Q) {
			variables.remove(q);
		}

		for (int v : variables.toArray())
			phi = phi.marginalize(v);

		return phi;
	}

}
