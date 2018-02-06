package ch.idsia.crema.factor;

import ch.idsia.crema.model.math.Operable;

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
	public F filter(int variable, int state);
	
	/** 
	 * Combine this factor with the provided one and return the 
	 * result as a new factor.
	 * 
	 * @param other
	 * @return
	 */
	@Override
	public F combine(F other);
	
	/**
	 * Sum out a variable from the factor.
	 * @param variable
	 * @return
	 */
	@Override
	public F marginalize(int variable);

	/**
	 * Divide this factor by the given one
	 * @param factor
	 * @return
	 */
	public F divide(F factor);
}
