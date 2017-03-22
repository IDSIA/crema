package ch.idsia.crema.user.credal;

import java.util.ArrayList;

import ch.idsia.crema.user.core.Variable;

public class SerparatelySpecifiedCredalFactor<CS extends CredalSet> {
	private Variable variable;
	private ArrayList<Variable> conditioning;
	private CS[] credalSets;

	public ArrayList<Variable> getConditioning() {
		return conditioning;
	}

	public void setConditioning(ArrayList<Variable> conditioning) {
		this.conditioning = conditioning;
		
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	
}
