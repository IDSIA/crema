package ch.idsia.crema.factor;

import ch.idsia.crema.core.Strides;

import java.util.Arrays;

public class VoidFactor implements Factor<VoidFactor> {

	private Strides domain;
	private long max;
	
	public VoidFactor(Strides stride, long lastmax) {
		this.domain = stride;
		this.max = Math.max(lastmax, domain.getCombinations());
	}

	/**
	 * Returns the maximum domain size encountered
	 * @return
	 */
	public long getMaxSize() {
		return max;
	}
	
	/**
	 * Factors are only mutable in their data. The domain should not change over time. We can therefore use 
	 * the original domain.
	 */
	@Override
	public VoidFactor copy() {
		return new VoidFactor(domain, max);
	}
	
	
	@Override
	public Strides getDomain() {
		return domain;
	}

	/**
	 * Reduce the domain by removing a variable and selecting the specified state.
	 * 
	 * @param variable the variable to be filtered out
	 * @param state the state to be selected
	 */
	@Override
	public VoidFactor filter(int variable, int state) {
		return new VoidFactor(domain.remove(variable), max);		
	}

	/**
	 * <p>Marginalize a variable out of the factor. This corresponds to sum all parameters that differ only in the state of the 
	 * marginalized variable.</p>
	 * 
	 * <p>If this factor represent a Conditional Probability Table you should only marginalize variables 
	 * on the right side of the conditioning bar. If so, there is no need for further normalization.</p>
	 * 
	 * @param variable the variable to be summed out of the CPT
	 * @return the new CPT with the variable marginalized out.
	 */
	@Override
	public VoidFactor marginalize(int variable) {
		return new VoidFactor(domain.remove(variable), max);
	}	
	
	/**
	 * The specialized method that avoids the cast of the input variable.
	 * 
	 * @param factor
	 * @return
	 */
	@Override
	public VoidFactor combine(final VoidFactor factor) {
		Strides newdomain = domain.union(factor.domain);
		return new VoidFactor(newdomain, max);
	}

	
	@Override
	public String toString() {
		return "P(" + Arrays.toString(domain.getVariables()) + ")";
	}


	@Override
	public VoidFactor divide(VoidFactor factor) {
		return this;
	}
}
