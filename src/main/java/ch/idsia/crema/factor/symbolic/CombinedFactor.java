package ch.idsia.crema.factor.symbolic;

public class CombinedFactor extends SymbolicAbstractFactor {

	private final SymbolicAbstractFactor factor1;
	private final SymbolicAbstractFactor factor2;

	public CombinedFactor(SymbolicAbstractFactor factor1, SymbolicAbstractFactor factor2) {
		super(factor1.getDomain().union(factor2.getDomain()));

		this.factor1 = factor1;
		this.factor2 = factor2;
	}

	
	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[]{factor1, factor2};
	}
}
