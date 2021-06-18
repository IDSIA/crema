package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.extensive.ExtensiveHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.solver.commons.Simplex;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SeparateLinearToExtensiveHalfspace {

	@Test
	public void testFromIntervalFactor1() {
		SeparateLinearToExtensiveHalfspaceFactor converter = new SeparateLinearToExtensiveHalfspaceFactor();

		IntervalFactor a = IntervalFactorFactory.factory()
				.domain(Strides.var(1, 2), Strides.var(0, 4))

				.lower(new double[]{.600, .375}, 0) // lP(Q=right|S=0)
				.lower(new double[]{.750, .225}, 1) // lP(Q=right|S=1)
				.lower(new double[]{.850, .125}, 2) // lP(Q=right|S=2)
				.lower(new double[]{.950, .025}, 3) // lP(Q=right|S=3)

				.upper(new double[]{.625, .400}, 0) // uP(Q=right|S=0)
				.upper(new double[]{.775, .250}, 1) // uP(Q=right|S=1)
				.upper(new double[]{.875, .150}, 2) // uP(Q=right|S=2)
				.upper(new double[]{.975, .050}, 3) // uP(Q=right|S=3)

				.get();

		ExtensiveHalfspaceFactor factor = converter.apply(a);
		//assertArrayEquals(new int[] {0,1}, factor.getDomain().getVariables());
		System.out.println(factor);

		Simplex simplex = new Simplex();
		simplex.loadProblem(factor, GoalType.MINIMIZE);
		simplex.solve(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
	}


	/**
	 * different variable order
	 */
	@Test
	public void testFromIntervalFactor2() {
		SeparateLinearToExtensiveHalfspaceFactor converter = new SeparateLinearToExtensiveHalfspaceFactor();

		final IntervalFactor fS = IntervalFactorFactory.factory().domain(Strides.var(0, 4), Strides.EMPTY)
				.lower(new double[]{.1, .3, .3, .1})
				.upper(new double[]{.2, .4, .4, .2})
				.get();

		ExtensiveHalfspaceFactor factor = converter.apply(fS);
		assertArrayEquals(new int[]{0}, factor.getDomain().getVariables());

		double[][] expected = new double[][]{
				new double[]{1, 0, 0, 0},
				new double[]{1, 0, 0, 0},
				new double[]{0, 1, 0, 0},
				new double[]{0, 1, 0, 0},
				new double[]{0, 0, 1, 0},
				new double[]{0, 0, 1, 0},
				new double[]{0, 0, 0, 1},
				new double[]{0, 0, 0, 1},
				new double[]{1, 1, 1, 1}
		};

		double[] values = new double[]{
				0.1, 0.2, 0.3, 0.4, 0.3, 0.4, 0.1, 0.2, 1
		};

		Relationship[] rels = new Relationship[]{
				Relationship.GEQ, Relationship.LEQ,
				Relationship.GEQ, Relationship.LEQ,
				Relationship.GEQ, Relationship.LEQ,
				Relationship.GEQ, Relationship.LEQ,
				Relationship.EQ
		};

		Collection<LinearConstraint> constraintSet = factor.getLinearProblem().getConstraints();
		int i = 0;
		for (LinearConstraint constraint : constraintSet) {
			assertArrayEquals(expected[i], constraint.getCoefficients().toArray(), 0.000001);
			assertEquals(rels[i], constraint.getRelationship());
			assertEquals(values[i], constraint.getValue());

			i = i + 1;
		}

		Simplex simplex = new Simplex();
		simplex.loadProblem(factor, GoalType.MAXIMIZE);
		simplex.solve(new double[]{1, 1, 0, 0}, 0.0);

		assertArrayEquals(new double[]{0.2, 0.4, 0.3, 0.1}, simplex.getVertex(), 1e-7);
	}


}
