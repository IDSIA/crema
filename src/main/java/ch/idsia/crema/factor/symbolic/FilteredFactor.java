package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.core.Strides;

public class FilteredFactor extends SymbolicFactor {

	private final int variable;
	private final int state;
	private final SymbolicFactor source;

	public FilteredFactor(SymbolicFactor source, int variable, int state) {
		super(new Strides(source.getDomain(), source.getDomain().indexOf(variable)));
		this.source = source;
		this.variable = variable;
		this.state = state;
	}

	public SymbolicFactor getSource() {
		return source;
	}

	public int getVariable() {
		return variable;
	}

	public int getState() {
		return state;
	}

}
