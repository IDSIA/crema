package ch.idsia.crema.factor;

import ch.idsia.crema.utility.ArraysUtil;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface OperableFactor<F extends OperableFactor<F>> extends FilterableFactor<F> {

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param other other factor to combine with this one
	 * @return a new factor combination of this with the given one
	 */
	F combine(F other);

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param others other factors to combine with this one
	 * @return a new factor, combination of this with the given others factors
	 */
	default F combine(F... others) {
		if (others.length < 1)
			return (F) this;

		F out = (F) this;
		for (F f : others) {
			out = out.combine(f);
		}
		return out;
	}

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param others collection of factors to combine with this one
	 * @return a new factor, combination of this with the given others factors
	 */
	default F combine(Collection<F> others) {
		return this.combine((F[]) others.toArray(OperableFactor[]::new));
	}

	/**
	 * Sum out a variable from the factor.
	 *
	 * @param variable variable to be marginalize out from this factor
	 * @return a new factor without the given variable
	 */
	F marginalize(int variable);

	/**
	 * Sum out a list of variables from the factor.
	 *
	 * @param variables variables to be marginalized out from this factor
	 * @return a new factor without the given variables
	 */
	default F marginalize(int... variables) {
		if (variables.length < 1)
			return (F) this;

		F out = (F) this;
		for (int v : variables) {
			out = out.marginalize(v);
		}
		return out;
	}

	/**
	 * @param factor given factor to divide by
	 * @return a new factor resulting in the division of this factor by the given one
	 */
	F divide(F factor);


	/**
	 * Factor normalization.
	 *
	 * @param given variables to not consider in the normalization
	 * @return a new factor where the probabilities are normalized and they sum up to 1.0
	 */
	default F normalize(int... given) {
		F div = (F) this;
		for (int m : ArraysUtil.removeAllFromSortedArray(getDomain().getVariables(), given)) {
			div = div.marginalize(m);
		}
		return divide(div);
	}

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
	@Override
	F filter(int variable, int state);

}
