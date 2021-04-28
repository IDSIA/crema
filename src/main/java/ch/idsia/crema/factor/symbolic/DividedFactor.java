package ch.idsia.crema.factor.symbolic;

public class DividedFactor extends SymbolicAbstractFactor {

	private final SymbolicAbstractFactor numerator;
	private final SymbolicAbstractFactor denominator;

	public DividedFactor(SymbolicAbstractFactor numerator, SymbolicAbstractFactor denominator) {
		super(numerator.getDomain());
		this.numerator = numerator;
		this.denominator = denominator;
	}

	public SymbolicAbstractFactor getNumerator() {
		return numerator;
	}

	public SymbolicAbstractFactor getDenominator() {
		return denominator;
	}

	@Override
	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[] { numerator, denominator };
	}
}
