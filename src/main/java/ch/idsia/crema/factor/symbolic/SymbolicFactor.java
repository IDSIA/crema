package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.Factor;


public abstract class SymbolicFactor implements Factor<SymbolicFactor> {

	private final Strides domain;

	public SymbolicFactor(Strides domain) {
		this.domain = domain;
	}

	@Override
	public CombinedFactor combine(SymbolicFactor other) {
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
	public SymbolicFactor copy() {
		// TODO: copy not this way
		return null;
	}

	@Override
	public FilteredFactor filter(int variable, int state) {
		return new FilteredFactor(this, variable, state);
	}

	@Override
	public DividedFactor divide(SymbolicFactor factor) {
		return new DividedFactor(this, factor);
	}
}
