package ch.idsia.crema.solver.commons;

import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;
import ch.idsia.crema.solver.LinearSolver;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An implementation of the LinearSolver interface based on apache's
 * {@link SimplexSolver}.
 *
 * @author huber
 */
public class Simplex implements LinearSolver {

	private final SimplexSolver solver;

	private LinearConstraintSet constraints;

	private PointValuePair solution;

	private GoalType goal;

	public Simplex() {
		solver = new SimplexSolver();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void loadProblem(ExtensiveLinearFactor factor, GoalType goal) {
		loadProblem(factor.getLinearProblem(), goal);
	}

	@Override
	public double getValue() {
		return solution.getValue();
	}

	@Override
	public double[] getVertex() {
		return solution.getPoint();
	}

	@Override
	public void solve(double[] objective, double constant) throws NoFeasibleSolutionException {
		try {
			solution = solver.optimize(constraints,
					new NonNegativeConstraint(true),
					new LinearObjectiveFunction(objective, constant),
					goal,
					PivotSelectionRule.BLAND);
		} catch (NoFeasibleSolutionException e) {
			System.err.println("WARNING:   Simplex\n" +
					"exception: " + e.getMessage() + "\n" +
					"goal:      " + goal + "\n" +
					"objective: " + Arrays.toString(objective) + "\n" +
					"constant:  " + constant + "\n" +
					"constraints:" + "\n" +
					constraints.getConstraints().stream()
							.map(c -> Arrays.toString(c.getCoefficients().toArray()) + " " + c.getRelationship() + " " + c.getValue())
							.collect(Collectors.joining("\n"))
			);
			throw e;
		}
	}

	@Override
	public void loadProblem(LinearConstraintSet data, GoalType goal) {
		this.goal = goal;
		this.constraints = data;
	}

	@Override
	public boolean isOptimal() {
		return Arrays.equals(solver.getLowerBound(), solver.getUpperBound());
	}
}
