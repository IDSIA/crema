package ch.idsia.crema.model.change;

import ch.idsia.crema.factor.GenericFactor;

/**
 * A simple implementation of a domain changer that will null every domain if its
 * domain is altered.
 * 
 * @author davidhuber
 *
 * @param <F>
 */
public class NullChange<F extends GenericFactor> implements DomainChange<F>, CardinalityChange<F> {
	
	public static final <F extends GenericFactor> NullChange<F> getInstance() {
		return new NullChange<F>();
	}
	
	@Override
	public F add(F factor, int variable) {
		return null;
	}
	
	@Override
	public F remove(F factor, int variable) {
		return null;
	}

	@Override
	public F addState(F factor, int variable) {
		return null;
	}

	@Override
	public F removeState(F factor, int variable, int state) {
		return null;
	}

	@Override
	public F addParentState(F factor, int variable, int parent) {
		return null;
	}
}
