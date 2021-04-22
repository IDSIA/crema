package ch.idsia.crema.factor.credal.linear.extensive;

import ch.idsia.crema.core.Strides;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * An extensive credal factor where the bounds are expressed as a set of Linear constraints
 * over the whole domain of the factor.
 *
 * @author david
 */
public class ExtensiveHalfspaceFactor implements ExtensiveLinearFactor<ExtensiveHalfspaceFactor> {

	private final Strides domain;
	private final List<LinearConstraint> data;

	public ExtensiveHalfspaceFactor(Strides domain) {
		data = new ArrayList<>();
		this.domain = domain;
	}

	public ExtensiveHalfspaceFactor(Strides domain, List<LinearConstraint> data) {
		this.data = data;
		this.domain = domain;
	}

	@Override
	public Strides getDomain() {
		return domain;
	}

	@Override
	public ExtensiveHalfspaceFactor copy() {
		List<LinearConstraint> newData = new ArrayList<>();
		for (LinearConstraint original_row : data) {
			LinearConstraint copy = new LinearConstraint(original_row.getCoefficients().copy(), original_row.getRelationship(), original_row.getValue());
			newData.add(copy);
		}
		return new ExtensiveHalfspaceFactor(domain, newData);
	}

	public void addConstraint(double[] data, Relationship rel, double value) {
		this.data.add(new LinearConstraint(data, rel, value));
	}

	public double[] getRandomVertex() {
		Random random = new Random();
		SimplexSolver solver = new SimplexSolver();

		double[] coeffs = new double[domain.getCombinations()];
		for (int i = 0; i < domain.getCombinations(); ++i) {
			coeffs[i] = random.nextDouble() + 1;
		}

		LinearObjectiveFunction c = new LinearObjectiveFunction(coeffs, 0);
		LinearConstraintSet Ab = new LinearConstraintSet(data);

		PointValuePair pvp = solver.optimize(Ab, c);
		return pvp.getPointRef();
	}

	@Override
	public LinearConstraintSet getLinearProblem() {
		return new LinearConstraintSet(data);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		for (LinearConstraint constraint : data) {
			buffer.append(Arrays.toString(constraint.getCoefficients().toArray()));
			buffer.append(constraint.getRelationship().toString());
			buffer.append(constraint.getValue());
			buffer.append("\n");
		}

		return buffer.toString();
	}
}