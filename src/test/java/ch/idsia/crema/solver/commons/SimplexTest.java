package ch.idsia.crema.solver.commons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.junit.Test;

public class SimplexTest {

	@Test
	public void test() {
		Simplex solver = new Simplex();
		ArrayList<LinearConstraint> constraints = new ArrayList<>();

		// -x0 + x1 <= 4
		LinearConstraint constraint = new LinearConstraint(new double[] { -1, 1 }, Relationship.LEQ, 4);
		constraints.add(constraint);

		// x1 <= 6
		constraint = new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 6);
		constraints.add(constraint);

		// x1, x0 >= 0 implicit in this fractional solver

		constraint = new LinearConstraint(new double[] { 2, 1 }, Relationship.LEQ, 14);
		constraints.add(constraint);

		double[] numerator = new double[] { -2, 1 };

		solver.loadProblem(new LinearConstraintSet(constraints), GoalType.MINIMIZE);
		solver.solve(numerator, 2);
		assertEquals(-12, solver.getValue(), 0.00000001);
		assertArrayEquals(new double[] { 7, 0 }, solver.getVertex(), 0.0000001);
	}

	@Test
	public void testLpSolveExample() {
		for (double i = 0; i < 10000; i += 1.09) {
			Simplex solver = new Simplex();
			ArrayList<LinearConstraint> constraints = new ArrayList<>();

			LinearConstraint constraint = new LinearConstraint(new double[] { 120, 210 }, Relationship.LEQ, 15000);
			constraints.add(constraint);

			constraint = new LinearConstraint(new double[] { 110, 30 }, Relationship.LEQ, 4000);
			constraints.add(constraint);

			constraint = new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 75);
			constraints.add(constraint);

			double[] numerator = new double[] { 143, 60 };

			solver.loadProblem(new LinearConstraintSet(constraints), GoalType.MAXIMIZE);

			solver.solve(numerator, i);

			assertEquals(6315.625 + i, solver.getValue(), 0.00000001);
			assertArrayEquals(new double[] { 21.875, 53.125 }, solver.getVertex(), 0.0000001);
		}
	}
}
