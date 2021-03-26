package ch.idsia.crema.solver.lpsolve;


import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplexTest {

	@Disabled // TODO
	@Test
	public void test() {
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
			assertArrayEquals(new double[] { 21.875, 53.125 }, solver.getVertex(), 1e-7);
		}
	}

}
