package ch.idsia.crema.factor.symbolic;

public class DividedFactor extends SymbolicFactor {

	private final SymbolicFactor factor1;
	private final SymbolicFactor factor2;

	public DividedFactor(SymbolicFactor numerator, SymbolicFactor denominator) {
		super(numerator.getDomain());
		this.factor1 = numerator;
		this.factor2 = denominator;
	}

	public SymbolicFactor[] getFactors() {
		return new SymbolicFactor[]{factor1, factor2};
	}
}
