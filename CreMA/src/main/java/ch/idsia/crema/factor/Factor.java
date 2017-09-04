package ch.idsia.crema.factor;

import ch.idsia.crema.model.math.Operable;

public interface Factor<F extends Factor<F>> extends FilterableFactor<F>, GenericFactor, Operable<F> {


	/** 
	 * Combine this factor with the provided one and return the 
	 * result as a new factor.
	 * 
	 * @param other the other factor
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
