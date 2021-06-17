package ch.idsia.crema.factor.symbolic.serialize;

import ch.idsia.crema.factor.symbolic.SymbolicFactor;

public interface SolverSerializer {

	/** 
	 * Serialize the problem to query the bound of the specified variable 
	 * @param target {@link SymbolicFactor} marginal or postirior on the desired target variable
	 */
	String serialize(SymbolicFactor target, int state, boolean maximize);
}
