package ch.idsia.crema.utility.hull;

import ch.idsia.crema.solver.commons.Simplex;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LPConvexHull {
	public static double[][] add(double[][] current, double[] newpoint) {

		ArrayList<double[]> newPoints = new ArrayList<>(Arrays.asList(current));
		newPoints.add(newpoint);

		// if new point is internal return
		if (isJasperInternal(newPoints, current.length)) return current;

		// else we keep testing
		for (int i = current.length - 1; i >= 0; i--) {
			if (isJasperInternal(newPoints, i)) {
				newPoints.remove(i);
			}
		}

		return newPoints.toArray(new double[newPoints.size()][]);
	}

	private static boolean isJasperInternal(List<double[]> points, int checkNr) {
		// check whether point nr. CheckNr is internal
		int nrPoints = points.size();
		int dimension = points.get(0).length;

		Simplex solver = new Simplex();
		List<LinearConstraint> constraints = new ArrayList<>();

		double[] coef = new double[nrPoints];
		for (int i = 0; i < nrPoints; i++) {
			coef[i] = 1;
		}
		LinearConstraint constraint = new LinearConstraint(coef, Relationship.EQ, 1);
		constraints.add(constraint);

		for (int j = 0; j < dimension; j++) {
			for (int i = 0; i < nrPoints; i++) {
				coef[i] = points.get(i)[j];
			}
			constraint = new LinearConstraint(coef, Relationship.EQ, points.get(checkNr)[j]);
			constraints.add(constraint);
		}

		// labmda_i >= 0 implicit in this fractional solver

		double[] optim = new double[nrPoints];
		for (int i = 0; i < nrPoints; i++) {
			optim[i] = 0;
		}
		optim[checkNr] = 1;

		solver.loadProblem(new LinearConstraintSet(constraints), GoalType.MINIMIZE);
		solver.solve(optim, 0);
		double solution = solver.getValue();

		boolean answer = false;
		if (solution <= 0.000001) {
			answer = true;
		}
		return answer;
	}

	private static boolean[] listJasperInternal(List<double[]> points) {
		int nrPoints = points.size();

		boolean[] list = new boolean[nrPoints];
		for (int i = 0; i < nrPoints; i++) {
			list[i] = isJasperInternal(points, i);
		}
		return list;
	}

	public static double[][] compute(double[][] points, boolean simplex) {
		int NrPoints = points.length;

		List<double[]> newPoints = new ArrayList<>(Arrays.asList(points));

		for (int i = NrPoints - 1; i >= 0; i--) {
			if (isJasperInternal(newPoints, i)) {
				newPoints.remove(i);
			}
		}
		return newPoints.toArray(new double[newPoints.size()][]);
	}
}
