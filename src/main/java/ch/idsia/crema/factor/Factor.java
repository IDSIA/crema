package ch.idsia.crema.factor;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.model.math.Operable;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;

public interface Factor<F extends Factor<F>> extends GenericFactor, Operable<F> {

	/**
	 * <p>
	 * Filter the factor by selecting only the values where the specified
	 * variable is in the specified state.
	 * </p>
	 *
	 * <p>
	 * Can return this if the variable is not part of the domain of the factor.
	 * </p>
	 *
	 * @param variable
	 * @param state
	 * @return
	 */
	F filter(int variable, int state);

	/**
	 * <p>
	 * Filter the factor by selecting only the values where the specified
	 * variable is in the specified state.
	 * </p>
	 *
	 * <p>
	 * Can return this if the variables are not part of the domain of the factor.
	 * </p>
	 *
	 * @param obs
	 * @return
	 */
	default F filter(TIntIntMap obs) {
		throw new NotImplementedException("Not Implemented yet");
	}

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param other
	 * @return
	 */
	@Override
	F combine(F other);

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param other
	 * @return
	 */
	default F combine(F... other) {
		if (other.length < 1)
			throw new IllegalArgumentException("wrong number of factors");

		F out = (F) this;
		for (F f : other) {
			out = out.combine(f);
		}
		return out;

	}

	@SuppressWarnings("unchecked")
	default F combine(Collection<F> other) {
		return this.combine((F[]) other.toArray(Factor[]::new));
	}

	/**
	 * Sum out a variable from the factor.
	 *
	 * @param variable
	 * @return
	 */
	@Override
	F marginalize(int variable);

	/**
	 * Sum out a list of variables from the factor.
	 *
	 * @param variables
	 * @return
	 */
	default F marginalize(int... variables) {
		if (variables.length < 1)
//			throw new IllegalArgumentException("wrong number of variables");
			return (F) this;

		F out = (F) this;
		for (int v : variables) {
			out = out.marginalize(v);
		}
		return out;
	}

	/**
	 * Divide this factor by the given one
	 *
	 * @param factor
	 * @return
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

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros).
	 * Thus, children variables are determined by the values of the parents
	 *
	 * @param left        Strides - children variables.
	 * @param right       Strides - parent variables
	 * @param assignments assignments of each combination of the parent
	 * @return
	 */
	static Factor deterministic(Strides left, Strides right, int... assignments) {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 *
	 * @param left       Strides - children variables.
	 * @param assignment int - single value to assign
	 * @return
	 */
	static Factor deterministic(Strides left, int assignment) {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Replaces the IDs of the variables in the domain
	 *
	 * @param new_vars
	 * @return
	 */
	default F renameDomain(int... new_vars) {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}

	default ObservationBuilder sample() {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}

}
