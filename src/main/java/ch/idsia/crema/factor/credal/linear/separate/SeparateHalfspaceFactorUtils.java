package ch.idsia.crema.factor.credal.linear.separate;

import ch.idsia.crema.core.Strides;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    26.04.2021 17:47
 */
public class SeparateHalfspaceFactorUtils {

	public static LinearConstraint[] buildConstraints(boolean normalized, boolean nonnegative, double[][] coefficients, double[] values, Relationship... rel) {
		int left_combinations = coefficients[0].length;

		List<LinearConstraint> C = new ArrayList<>();

		// check the coefficient shape
		for (double[] c : coefficients) {
			if (c.length != left_combinations)
				throw new IllegalArgumentException("Wrong coefficient matrix shape");
		}

		// check the relationship vector length
		if (rel.length == 0)
			rel = new Relationship[]{Relationship.EQ};
		if (rel.length == 1) {
			Relationship[] rel_aux = new Relationship[coefficients.length];
			for (int i = 0; i < coefficients.length; i++)
				rel_aux[i] = rel[0];
			rel = rel_aux;
		} else if (rel.length != coefficients.length) {
			throw new IllegalArgumentException("Wrong relationship vector length: " + rel.length);
		}

		for (int i = 0; i < coefficients.length; i++) {
			C.add(new LinearConstraint(coefficients[i], rel[i], values[i]));
		}

		// normalization constraint
		if (normalized) {
			double[] ones = new double[left_combinations];
			Arrays.fill(ones, 1.);
			C.add(new LinearConstraint(ones, Relationship.EQ, 1.0));
		}

		// non-negative constraints
		if (nonnegative) {
			double[] zeros = new double[left_combinations];
			for (int i = 0; i < left_combinations; i++) {
				double[] c = ArrayUtils.clone(zeros);
				c[i] = 1.;
				C.add(new LinearConstraint(c, Relationship.GEQ, 0));
			}
		}

		return C.toArray(LinearConstraint[]::new);
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros).
	 * Thus, children variables are determined by the values of the parents
	 *
	 * @param left        Strides - children variables.
	 * @param right       Strides - parent variables
	 * @param assignments assignments of each combination of the parent
	 * @return
	 */
	public static SeparateHalfspaceDefaultFactor deterministic(Strides left, Strides right, int... assignments) {
		if (assignments.length != right.getCombinations())
			throw new IllegalArgumentException("ERROR: length of assignments should be equal to the number of combinations of the parents");

		if (Ints.min(assignments) < 0 || Ints.max(assignments) >= left.getCombinations())
			throw new IllegalArgumentException("ERROR: assignments of deterministic function should be in the inteval [0," + left.getCombinations() + ")");

		SeparateHalfspaceDefaultFactor f = new SeparateHalfspaceDefaultFactor(left, right);

		int left_combinations = left.getCombinations();

		for (int i = 0; i < right.getCombinations(); i++) {
			double[][] coeff = new double[left_combinations][left_combinations];
			for (int j = 0; j < left_combinations; j++) {
				coeff[j][j] = 1.;
			}
			double[] values = new double[left_combinations];
			values[assignments[i]] = 1.;


			// Build the constraints
			LinearConstraint[] C = buildConstraints(true, true, coeff, values, Relationship.EQ);

			// Add the constraints
			for (LinearConstraint c : C) {
				f.addConstraint(c, i);
			}
		}

		return f;
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 *
	 * @param left       Strides - children variables.
	 * @param assignment int - single value to assign
	 * @return
	 */
	public static SeparateHalfspaceDefaultFactor deterministic(Strides left, int assignment) {
		return deterministic(left, Strides.empty(), assignment);
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 *
	 * @param domain     domain to interesct with var
	 * @param var        int - id for the single children variable.
	 * @param assignment int - single value to assign
	 * @return
	 */
	public SeparateHalfspaceDefaultFactor getDeterministic(Strides domain, int var, int assignment) {
		return deterministic(domain.intersection(var), assignment);
	}

}
