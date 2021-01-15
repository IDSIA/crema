package ch.idsia.crema.factor.symbolic;

public class CombinedFactor extends SymbolicFactor {

	private final SymbolicFactor factor1;
	private final SymbolicFactor factor2;

	public CombinedFactor(SymbolicFactor factor1, SymbolicFactor factor2) {
		super(factor1.getDomain().union(factor2.getDomain()));

		this.factor1 = factor1;
		this.factor2 = factor2;
	}

	public SymbolicFactor[] getFactors() {
		return new SymbolicFactor[]{factor1, factor2};
	}
}
