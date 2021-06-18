package ch.idsia.crema.factor;

import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.03.2021 09:49
 */
public interface FilterableFactor<F extends FilterableFactor<F>> extends GenericFactor {

	/**
	 * <p>
	 * Filter the factor by selecting only the values where the specified
	 * variable is in the specified state.
	 * </p>
	 *
	 * <p>
	 * Should return this if the variable is not part of the domain of the factor.
	 * </p>
	 *
	 * @param variable variable to filter
	 * @param state    state of the variable to filter
	 * @return a new factor where the given variable in the given state has been filtered out
	 */
	F filter(int variable, int state);

	/**
	 * <p>
	 * Filter the factor by selecting only the values where the specified
	 * variable is in the specified state.
	 * </p>
	 *
	 * <p>
	 * Should return this if the variables are not part of the domain of the factor.
	 * </p>
	 *
	 * @param obs a variable-state map of the observed evidence
	 * @return a new factor where the given variable in the given state has been filtered out
	 */
	@SuppressWarnings("unchecked")
	default F filter(TIntIntMap obs) {
		F f = (F) this.copy();
		for (int v : obs.keys()) {
			if (ArraysUtil.contains(v, f.getDomain().getVariables()))
				f = f.filter(v, obs.get(v));
		}
		return f;
	}

}
