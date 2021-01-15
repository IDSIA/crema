package ch.idsia.crema.model.math;

import ch.idsia.crema.factor.GenericFactor;

/**
 * A class that is capable of two operations. Kinda like {@link Comparable}.
 *
 * @author david
 */
public interface Operable<F extends GenericFactor> {

	/**
	 * A method that combine this factor with the given one and returns a new
	 * factor.
	 *
	 * @param other
	 * @return
	 */
	F combine(F other);

	/**
	 * A method that marginalizes a varibale out of this factor and returns a new
	 * factor. Implementations can return this if var is not in this domain.
	 *
	 * @param var
	 * @return
	 */
	F marginalize(int var);
}
