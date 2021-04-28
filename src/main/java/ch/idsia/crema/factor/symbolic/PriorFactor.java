package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.factor.GenericFactor;

import java.util.Arrays;

public class PriorFactor extends SymbolicAbstractFactor {

	private final GenericFactor factor;

	public PriorFactor(GenericFactor lf) {
		super(lf.getDomain());
		this.factor = lf;
	}

	@Override
	public PriorFactor copy() {
		return new PriorFactor(factor);
	}

	public GenericFactor getFactor() {
		return factor;
	}

	@Override
	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[0];
	}

	@Override
	public String toString() {
		return String.format("P(%s)", Arrays.toString(getDomain().getVariables()));
	}
}
