package ch.idsia.crema.solver;

import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public interface LinearFractionalSolver {
	public void loadProblem(ExtensiveLinearFactor factor, GoalType type);

	public void loadProblem(LinearConstraintSet data, GoalType type);

	public void solve(double[] numerator, double alpha, double[] denominator, double beta);
	
	public void solve(double[] numerator, double alpha, double[] denominator, double beta, double mult);
	
	public double getValue();
	
	public double[] getVertex();
}
