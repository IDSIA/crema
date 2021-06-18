package ch.idsia.crema.factor.algebra;

import ch.idsia.crema.factor.OperableFactor;

/**
 * A class managing operation on factors.
 *
 * @param <F>
 * @author david
 */
public interface Operation<F extends OperableFactor<F>> {

	/**
	 * Combine two or more factors. The actual operation and the access to the information
	 * is left to the actaul implementation.
	 *
	 * @param f1
	 * @param f2
	 * @return
	 */
	F combine(F f1, F f2);

	/**
	 * Marginalize the specified variable from the factor.
	 *
	 * @param one
	 * @param variable
	 * @return
	 */
	F marginalize(F one, int variable);

	/**
	 * Divide the specified factors with eachothers.
	 *
	 * @param one
	 * @param other
	 * @return
	 */
	F divide(F one, F other);

	/**
	 * Select the subfactor of factor one where the specified variable is in state "state".
	 *
	 * @param one
	 * @param variable
	 * @return
	 */
	F filter(F one, int variable, int state);

}
