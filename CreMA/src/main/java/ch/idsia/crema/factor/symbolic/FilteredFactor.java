package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.model.Strides;

public class FilteredFactor extends SymbolicFactor {
	private int variable; 
	private int state;
	private SymbolicFactor source;
	
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
