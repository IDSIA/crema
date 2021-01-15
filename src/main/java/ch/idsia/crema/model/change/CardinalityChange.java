package ch.idsia.crema.model.change;

import ch.idsia.crema.factor.GenericFactor;

public interface CardinalityChange<F extends GenericFactor> {
	/**
	 * Update the given factor by adding a new state to the specified variable.
	 * Implementations are allowed, but not required, to perform the change inline.
	 *
	 * @param factor
	 * @param variable
	 * @return
	 */
	F addState(F factor, int variable);

	/**
	 * Update the factor by removing the specified state from the variable.
	 * Implementations are allowed, but not required, to perform the change inline.
	 *
	 * @param factor
	 * @param variable
	 * @param state
	 * @return
	 */
	F removeState(F factor, int variable, int state);

	/**
	 * Handle changes to the factor caused by the addition of a state to a parent variable
	 *
	 * @param factor
	 * @param variable
	 * @param parent
	 * @return
	 */
	F addParentState(F factor, int variable, int parent);
}
