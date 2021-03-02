package ch.idsia.crema.solver;

import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

public interface LinearFractionalSolver {

	@SuppressWarnings("rawtypes")
	void loadProblem(ExtensiveLinearFactor factor, GoalType type);

	void loadProblem(LinearConstraintSet data, GoalType type);

	void solve(double[] numerator, double alpha, double[] denominator, double beta);
	
	void solve(double[] numerator, double alpha, double[] denominator, double beta, double mult);
	
	double getValue();
	
	double[] getVertex();
}
