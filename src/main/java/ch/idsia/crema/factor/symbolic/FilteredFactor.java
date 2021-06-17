package ch.idsia.crema.factor.symbolic;

public class FilteredFactor extends SymbolicAbstractFactor {

	private final int variable;
	private final int state;
	private final SymbolicFactor factor;

	public FilteredFactor(SymbolicFactor factor, int variable, int state) {
		super(factor.getDomain().remove(variable));

		this.factor = factor;
		this.variable = variable;
		this.state = state;
	}

	@Override
	public FilteredFactor copy() {
		return new FilteredFactor(factor, variable, state);
	}

	public SymbolicFactor getFactor() {
		return factor;
	}

	public int getVariable() {
		return variable;
	}

	public int getState() {
		return state;
	}

	@Override
	public SymbolicFactor[] getSources() {
		return new SymbolicFactor[]{factor};
	}

	@Override
	public String toString() {
		return String.format("%s.filter(%d, %d)", factor, variable, state);
	}

}
