package ch.idsia.crema.factor.credal.linear;

import org.apache.commons.math3.optim.linear.LinearConstraintSet;

/**
 * An extensively specified factor based representable as a set of linear constraints 
 * over the complete domain.
 * 
 * @author david
 *
 * @param <F> The type managed by the implementing class. Usually it is the class itself.
 */
public interface ExtensiveLinearFactor<F extends ExtensiveLinearFactor<F>> extends LinearFactor {

	/**
	 * Convert the factor to a single set of LinearProblems. The problem space will be over the 
	 * whole combination of states of the domain.
	 * 
	 * @return
	 */
	public LinearConstraintSet getLinearProblem();
}
