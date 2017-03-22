package ch.idsia.crema.model.math;

import ch.idsia.crema.factor.GenericFactor;

/**
 * A class managing operation on objects.
 * 
 * @author david
 *
 * @param <F>
 */
public interface Operation<F extends GenericFactor> {

	/**
	 * Combine two or more factors. The actual operation and the access to the information
	 * is left to the actaul implementation.
	 * 
	 * @param one
	 * @param others 
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
	 * @param variable
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
