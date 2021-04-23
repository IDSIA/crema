package ch.idsia.crema.jasper;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.ExtensiveVertexDefaultAlgebra;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactorFactory;
import ch.idsia.crema.solver.commons.Simplex;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JaspersTests {

	public boolean isInternal(List<double[]> Points, int CheckNr) {
		//check whether point nr. CheckNr is internal
		int NrPoints = Points.size();
		int Dimension = Points.get(0).length;

		Simplex solver = new Simplex();
		List<LinearConstraint> constraints = new ArrayList<>();

		double[] coef = new double[NrPoints];
		for (int i = 0; i < NrPoints; i++) {
			coef[i] = 1;
		}
		LinearConstraint constraint = new LinearConstraint(coef, Relationship.EQ, 1);
		constraints.add(constraint);

		for (int j = 0; j < Dimension; j++) {
			for (int i = 0; i < NrPoints; i++) {
				coef[i] = Points.get(i)[j];
			}
			constraint = new LinearConstraint(coef, Relationship.EQ, Points.get(CheckNr)[j]);
			constraints.add(constraint);
		}

		// labmda_i >= 0 implicit in this fractional solver

		double[] optim = new double[NrPoints];
		for (int i = 0; i < NrPoints; i++) {
			optim[i] = 0;
		}
		optim[CheckNr] = 1;

		solver.loadProblem(new LinearConstraintSet(constraints), GoalType.MINIMIZE);
		solver.solve(optim, 0);
		double solution = solver.getValue();

		boolean answer = false;
		if (solution <= 0.000001) {
			answer = true;
		}
		return answer;
	}

	public boolean[] listInternal(List<double[]> Points) {

		int NrPoints = Points.size();

		boolean[] list = new boolean[NrPoints];
		for (int i = 0; i < NrPoints; i++) {
			list[i] = isInternal(Points, i);
		}
		return list;
	}

	public List<double[]> naiveRemoveInternal(List<double[]> Points) {
		int NrPoints = Points.size();
		for (int i = NrPoints - 1; i >= 0; i--) {
			if (isInternal(Points, i)) {
				Points.remove(i);
			}
		}
		return Points;
	}

	@Test
	public void firstTest() {

		// factor (v0 | v2)
		Strides domain = DomainBuilder.var(0, 2).size(3, 3).strides();
		ExtensiveVertexFactor factor = ExtensiveVertexFactorFactory.factory().domain(domain)
				// populate with some data
				.addVertex(new double[]{0.1, 0.2, 0.7, 0.3, 0.3, 0.4, 0.4, 0.5, 0.1})
				.addVertex(new double[]{0.2, 0.6, 0.2, 0.4, 0.2, 0.4, 0.1, 0.1, 0.8})
				.addVertex(new double[]{0.3, 0.6, 0.1, 0.6, 0.2, 0.2, 0.4, 0.1, 0.5})
				.get();

		Strides domain2 = DomainBuilder.var(2).size(3).strides();
		ExtensiveVertexFactor factor2 = ExtensiveVertexFactorFactory.factory().domain(domain2)
				.addVertex(new double[]{0.1, 0.1, 0.8})
				.addVertex(new double[]{0.3, 0.4, 0.3})
				.addVertex(new double[]{0.5, 0.3, 0.2})
				.addVertex(new double[]{0.7, 0.2, 0.1})
				.get();

		ExtensiveVertexDefaultAlgebra algebra = new ExtensiveVertexDefaultAlgebra();

		ExtensiveVertexFactor f3 = algebra.combine(factor, factor2);

		// since we have no convex hull yet we will end up with many vertices
		// to be exact 3 * 4
		//assertEquals(12, f3.getInternalVertices().size());

		ExtensiveVertexFactor f4 = algebra.marginalize(f3, 2);
		//assertEquals(12, f4.getInternalVertices().size());

		List<double[]> Points = f4.getInternalVertices();
		int NrPoints = Points.size();
		//int Dimension = Points.get(0).length;
		//int CheckNr = 0;

		//boolean answer = isInternal(Points,CheckNr);
		//System.out.println("single internalcheck");
		//System.out.println(answer);

		//boolean[] list = listInternal(Points);
		//System.out.println("List of internalchecks:");
		//for(int i=0;i<NrPoints;i++){
		//	System.out.println(list[i]);
		//}

		System.out.println("Number of Points:");
		System.out.println(NrPoints);
		System.out.println("Points:");
		for (double[] point : Points) {
			System.out.println(java.util.Arrays.toString(point));
		}

		List<double[]> ExtremePoints = naiveRemoveInternal(Points);
		int NrExtremePoints = ExtremePoints.size();
		System.out.println("Number of Extreme Points:");
		System.out.println(NrExtremePoints);
		System.out.println("Extreme Points:");
		for (double[] extremePoint : ExtremePoints) {
			System.out.println(java.util.Arrays.toString(extremePoint));
		}

		//assertEquals(-12, solver.getValue(), 0.00000001);
		//assertArrayEquals(new double[] { 7, 0 }, solver.getVertex(), 1e-7);

		// marginalize everything and we have to end up with [1]
		//ExtensiveVertexFactor f5 = algebra.marginalize(f4, 0);

		//assertEquals(12, f5.getInternalVertices().size());
		//for (double[] data : f5.getInternalVertices()) {
		//	assertArrayEquals(new double[] { 1 }, data, 0.0000000001);
		//}
	}
}
