package ch.idsia.crema.model.change;

import ch.idsia.crema.factor.GenericFactor;

public class VoidChange<F extends GenericFactor> implements DomainChange<F>, CardinalityChange<F> {

	@Override
	public F addState(F factor, int variable) {
		return factor;
	}

	@Override
	public F removeState(F factor, int variable, int state) {
		return factor;
	}

	@Override
	public F addParentState(F factor, int variable, int parent) {
		return factor;
	}

	@Override
	public F add(F factor, int variable) {
		return factor;
	}

	@Override
	public F remove(F factor, int variable) {
		return factor;
	}

}
