package ch.idsia.crema.core;


public interface Domain {

	/**
	 * Get the cardinality of a variable in the domain
	 *
	 * @param variable
	 * @return
	 */
	int getCardinality(int variable);

	/**
	 * <p>Get the cardinality of the variable at the specified offset in the domain.</p>
	 * <p>This method is usually used with the result of {@link #indexOf}.</p>
	 *
	 * @param index
	 * @return
	 */
	int getSizeAt(int index);

	/**
	 * Find the location/offset of a variable in the domain.
	 *
	 * @param variable
	 * @return
	 */
	int indexOf(int variable);

	/**
	 * Check if the specified variable is present in the domain
	 *
	 * @param variable
	 * @return
	 */
	boolean contains(int variable);

	/**
	 * The vector of variables in this domain.
	 * Please read-only!
	 *
	 * @return
	 */
	int[] getVariables();

	/**
	 * Get all the cardinalities of the variables in the domain.
	 *
	 * @return
	 */
	int[] getSizes();

	/**
	 * Get the number of variables in the domain.
	 *
	 * @return
	 */
	int getSize();

	/**
	 * Notify the domain that a variable has been removed. Prior to remove a variable the model will
	 * have to remove all arcs involving the variable. Because of this domains should not contain the
	 * removed variable.
	 *
	 * <p>An {@link IllegalStateException} may be thrown if the removed variable is part of the domain </p>
	 *
	 * @param variable
	 */
	void removed(int variable);
}
