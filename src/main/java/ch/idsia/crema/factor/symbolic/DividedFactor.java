package ch.idsia.crema.factor.symbolic;

public class DividedFactor extends SymbolicFactor {

	private final SymbolicFactor numerator;
	private final SymbolicFactor denominator;

	public DividedFactor(SymbolicFactor numerator, SymbolicFactor denominator) {
		super(numerator.getDomain());
		this.numerator = numerator;
		this.denominator = denominator;
	}

	public SymbolicFactor getNumerator() { 
		return numerator;
	}

	public SymbolicFactor getDenominator() { 
		return denominator;
	}

	@Override
	public SymbolicFactor[] getSources() {
		return new SymbolicFactor[] { numerator, denominator };
	}
}
