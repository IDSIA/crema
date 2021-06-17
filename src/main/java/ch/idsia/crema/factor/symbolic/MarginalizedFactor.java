package ch.idsia.crema.factor.symbolic;

public class MarginalizedFactor extends SymbolicAbstractFactor {

	private final SymbolicAbstractFactor factor;
	private final int variable;

	public MarginalizedFactor(SymbolicAbstractFactor factor, int variable) {
		super(factor.getDomain().removeAt(factor.getDomain().indexOf(variable)));

		this.factor = factor;
		this.variable = variable;
	}

	@Override
	public SymbolicFactor copy() {
		return new MarginalizedFactor(factor, variable);
	}

	public SymbolicAbstractFactor getFactor() {
		return factor;
	}

	public int getVariable() {
		return variable;
	}

	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[]{factor};
	}

	@Override
	public String toString() {
		return String.format("%s.marginalize(%d)", factor, variable);
	}
}
