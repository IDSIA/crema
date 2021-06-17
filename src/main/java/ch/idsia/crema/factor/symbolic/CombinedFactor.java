package ch.idsia.crema.factor.symbolic;

public class CombinedFactor extends SymbolicAbstractFactor {

	private final SymbolicFactor factor1;
	private final SymbolicFactor factor2;

	public CombinedFactor(SymbolicFactor factor1, SymbolicFactor factor2) {
		super(factor1.getDomain().union(factor2.getDomain()));

		this.factor1 = factor1;
		this.factor2 = factor2;
	}

	@Override
	public CombinedFactor copy() {
		return new CombinedFactor(factor1, factor2);
	}

	public SymbolicFactor getFactor1() {
		return factor1;
	}

	public SymbolicFactor getFactor2() {
		return factor2;
	}

	public SymbolicFactor[] getSources() {
		return new SymbolicFactor[]{factor1, factor2};
	}

	@Override
	public String toString() {
		return String.format("%s.combine(%s)", factor1, factor2);
	}

}
