package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.factor.GenericFactor;

public class PriorFactor extends SymbolicAbstractFactor {

	private final GenericFactor factor;

	public PriorFactor(GenericFactor lf) {
		super(lf.getDomain());
		this.factor = lf;
	}

	public GenericFactor getFactor() {
		return factor;
	}

	@Override
	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[0];
	}
}
