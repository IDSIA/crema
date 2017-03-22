package ch.idsia.crema.solver;

import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;

/**
 * A linear programming solver's minimal required interface for CreMA's needs. 
 * 
 * @author huber
 *
 */
public interface LinearSolver {
	
	/**
	 * Load the constraints from the specified {@link ExtensiveLinearFactor}.
	 * 
	 * @param factor - the set of constraints as an {@link ExtensiveLinearFactor}.
	 * @param goal - {@link GoalType} the direction of the optimization.
	 */
	@SuppressWarnings("rawtypes")
	public void loadProblem(ExtensiveLinearFactor factor, GoalType goal);
	
	
	/**
	 * Load the constraints from the specified {@link LinearConstraintSet}.
	 * 
	 * @param factor - the set of constraints as an {@link LinearConstraintSet}.
	 * @param goal - {@link GoalType} the direction of the optimization.
	 */
	public void loadProblem(LinearConstraintSet data, GoalType goal);
	
	/**
	 * Start the solver with the specified objective function and constant term
	 * 
	 * @param objective
	 * @param constant
	 */
	public void solve(double[] objective, double constant);
	
	/**
	 * Get the objective function's value 
	 * @return
	 */
	public double getValue();
	
	/**
	 * Get the vertex of the found solution.
	 * 
	 * @return
	 */
	public double[] getVertex();
	
	/**
	 * Return whether the found solution is optimal.
	 * 
	 * @return
	 */
	public boolean isOptimal();
}
