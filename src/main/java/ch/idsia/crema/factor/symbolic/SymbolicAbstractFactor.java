package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.core.Strides;


public abstract class SymbolicAbstractFactor implements SymbolicFactor {

	private final Strides domain;

	public SymbolicAbstractFactor(Strides domain) {
		this.domain = domain;
	}

	@Override
	public Strides getDomain() {
		return domain;
	}

	@Override
	public SymbolicAbstractFactor combine(SymbolicFactor other) {
		return new CombinedFactor(this, other);
	}

	@Override
	public SymbolicAbstractFactor marginalize(int variable) {
		return new MarginalizedFactor(this, variable);
	}

	@Override
	public SymbolicAbstractFactor filter(int variable, int state) {
		return new FilteredFactor(this, variable, state);
	}

	@Override
	public SymbolicAbstractFactor divide(SymbolicFactor factor) {
		return new DividedFactor(this, factor);
	}

	@Override
	public SymbolicFactor normalize(int... given) {
		return new NormalizedFactor(this, given);
	}

	/**
	 * @return Return the factors that originated this factor
	 */
	public abstract SymbolicFactor[] getSources();
}
