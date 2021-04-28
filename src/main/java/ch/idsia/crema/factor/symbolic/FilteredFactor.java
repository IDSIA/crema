package ch.idsia.crema.factor.symbolic;

public class FilteredFactor extends SymbolicAbstractFactor {

	private final int variable;
	private final int state;
	private final SymbolicAbstractFactor source;

	public FilteredFactor(SymbolicAbstractFactor source, int variable, int state) {
		// super(new Strides(source.getDomain(), source.getDomain().indexOf(variable)));
		super(source.getDomain().remove(variable));
		
		this.source = source;
		this.variable = variable;
		this.state = state;
	}

	public SymbolicAbstractFactor getSource() {
		return source;
	}

	public int getVariable() {
		return variable;
	}

	public int getState() {
		return state;
	}

	@Override
	public SymbolicAbstractFactor[] getSources() {
		return new SymbolicAbstractFactor[] { source };
	}
}
