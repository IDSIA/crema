package ch.idsia.crema.factor;

import ch.idsia.crema.factor.operations.Operable;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface OperableFactor<F extends OperableFactor<F>> extends FilterableFactor<F>, Operable<F> {

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param other other factor to combine with this one
	 * @return a new factor combination of this with the given one
	 */
	@Override
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
	@Override
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
	 */
	default F normalize(int... given) {
		F div = (F) this;
		for (int m : ArraysUtil.removeAllFromSortedArray(getDomain().getVariables(), given)) {
			div = div.marginalize(m);
		}
		return divide(div);
	}

}
