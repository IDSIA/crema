package ch.idsia.crema.factor.symbolic;

public class MarginalizedFactor extends SymbolicFactor {
	private SymbolicFactor source;
	private int variable;
	
	public MarginalizedFactor(SymbolicFactor factor, int variable) {
		super(factor.getDomain().removeAt(factor.getDomain().indexOf(variable)));
		
		this.source = factor;
		this.variable = variable;
	}
	
	public SymbolicFactor getSource() {
		return source;
	}
	
	public int getVariable() {
		return variable;
	}
}
