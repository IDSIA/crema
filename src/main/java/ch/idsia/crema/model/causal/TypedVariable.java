package ch.idsia.crema.model.causal;

import ch.idsia.crema.core.Variable;
import ch.idsia.crema.model.causal.SCM.VariableType;

public class TypedVariable extends Variable {
	VariableType type;

	public TypedVariable(int label, int cardinality, VariableType type) {
		super(label, cardinality);
		this.type = type;
	}
	
	public VariableType getType() {
		return type;
	}
}
