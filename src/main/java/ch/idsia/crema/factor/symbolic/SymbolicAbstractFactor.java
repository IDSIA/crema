package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.OperableFactor;


public abstract class SymbolicAbstractFactor implements OperableFactor<SymbolicAbstractFactor> {

	private final Strides domain;

	public SymbolicAbstractFactor(Strides domain) {
		this.domain = domain;
	}

	@Override
	public CombinedFactor combine(SymbolicAbstractFactor other) {
		return new CombinedFactor(this, other);
	}

	@Override
	public MarginalizedFactor marginalize(int variable) {
		return new MarginalizedFactor(this, variable);
	}

	@Override
	public Strides getDomain() {
		return domain;
	}

	@Override
	public SymbolicAbstractFactor copy() {
		// TODO: copy not this way
		return null;
	}

	@Override
	public FilteredFactor filter(int variable, int state) {
		return new FilteredFactor(this, variable, state);
	}

	@Override
	public DividedFactor divide(SymbolicAbstractFactor factor) {
		return new DividedFactor(this, factor);
	}

	/**
	 * Return the factors that originated this factor. 
	 * @return
	 */
	public abstract SymbolicAbstractFactor[] getSources();
}
