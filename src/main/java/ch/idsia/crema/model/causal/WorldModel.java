package ch.idsia.crema.model.causal;

import ch.idsia.crema.core.Variable;

public interface WorldModel {
	/**
	 * Get the global SCM model
	 * @return SCM the global model or null if no model have been added
	 */
	public SCM get();
		
	/**
	 * Add a model to the mapping assuming exogenous ids match. 
	 * 
	 * @param model the {@link SCM} model to be added
	 * @return int the id of the added worlds
	 */
	public int add(SCM model);
		
	/**
	 * Translate a local variable of world wid to the global id
	 * 
	 * @param variable the local variable
	 * @param wid the id of the source world
	 * @return int the global id of the variable
	 */
	public int toGlobal(int variable, int wid);
	
	/** 
	 * Translate a global id to a local one. 
	 * 
	 * @param variable the global variable id
	 * @return int a local variable id
	 */
	public int fromGlobal(int variable);

	/**
	 * Get the world id associated to the specified global variable id. 
	 * For shared variables (i.e. exogenous) the method return -1;
	 * 
	 * @param variable the global variable id
	 * @return int the world id of the specified global variable or -1 it exogenous
	 */
	public int worldIdOf(int variable);
	
	/**
	 * Get the world associated to the specified global variable id. 
	 * The method returns null for shared (i.e. exogenous) variables.
	 * 
	 * @param variable the global variable id
	 * @return SCM the source Structural Causal Model or null if variable is exogenous
	 */
	public SCM worldOf(int variable);
	
	default int[] toGlobal(int[] variables, int wid) {
		int[] result = new int[variables.length];
		for (int i = 0; i < result.length; ++i) {
			result[i] = toGlobal(variables[i], wid);
		}
		return result;
	}
	
	default int[] fromGlobal(int[] variables) {
		int[] result = new int[variables.length];
		for (int i = 0; i < result.length; ++i) {
			result[i] = fromGlobal(variables[i]);
		}
		return result;
	}	
}
