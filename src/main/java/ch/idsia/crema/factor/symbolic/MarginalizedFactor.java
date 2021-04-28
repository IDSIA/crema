package ch.idsia.crema.factor.symbolic;

public class MarginalizedFactor extends SymbolicAbstractFactor {

	private final SymbolicAbstractFactor source;
	private final int variable;

	public MarginalizedFactor(SymbolicAbstractFactor factor, int variable) {
		super(factor.getDomain().removeAt(factor.getDomain().indexOf(variable)));

		this.source = factor;
		this.variable = variable;
	}

	public SymbolicAbstractFactor getSource() {
		return source;
	}

	public int getVariable() {
		return variable;
	}

	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[] { source };
	}
}
