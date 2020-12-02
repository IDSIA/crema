package ch.idsia.crema.model.change;

import ch.idsia.crema.factor.GenericFactor;

/**
 * This class can be used to set how a factor should change when a parent is added to
 * a variable.
 * <p>
 * The default implementation is to nullify the factor.
 *
 * @author david
 */
public interface DomainChange<F extends GenericFactor> {

	/**
	 * Add a variable to the domain of a Factor. In directed models this is triggered upon
	 * adding a parent to a variable. Implementations are allowed, but not required,
	 * to perform the change inline.
	 *
	 * @param factor   the affected factor (NULL ok)
	 * @param variable
	 * @return the new updated factor (May be NULL)
	 */
	F add(F factor, int variable);

	/**
	 * Remove a variable from the domain of a factor. Implementations are allowed, but not required,
	 * to perform the change inline.
	 *
	 * @param factor
	 * @param variable
	 * @return
	 */
	F remove(F factor, int variable);

}
