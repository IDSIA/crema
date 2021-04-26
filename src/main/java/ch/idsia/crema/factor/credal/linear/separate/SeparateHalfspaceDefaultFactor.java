package ch.idsia.crema.factor.credal.linear.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ConstraintsUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;

import java.util.*;

/**
 * A separately specified Credal factor that has a list of linear constrains for each
 * separation.
 *
 * @author huber
 */
public class SeparateHalfspaceDefaultFactor extends SeparateHalfspaceAbstractFactor<SeparateHalfspaceDefaultFactor> implements SeparateLinearFactor<SeparateHalfspaceDefaultFactor> {

	private List<List<LinearConstraint>> data;

	public SeparateHalfspaceDefaultFactor(Strides content, Strides separation) {
		super(content, separation);

		data = new ArrayList<>(separation.getCombinations());
		for (int i = 0; i < separation.getCombinations(); i++) {
			data.add(new ArrayList<>());
		}
	}

	public SeparateHalfspaceDefaultFactor(Strides content, Strides separation, List<List<LinearConstraint>> data) {
		super(content, separation);
		this.data = data;
	}

	public SeparateHalfspaceDefaultFactor(Strides left, double[][] coefficients, double[] values, Relationship... rel) {
		this(left, Strides.empty());

		// Build the constraints (including non-negative constraints)
		LinearConstraint[] C = buildConstraints(true, true, coefficients, values, rel);

		// add constraints to this factor
		for (LinearConstraint c : C) {
			this.addConstraint(c);
		}
	}

	public SeparateHalfspaceDefaultFactor(boolean normalized, boolean nonnegative, Strides left, double[][] coefficients, double[] values, Relationship... rel) {
		this(left, Strides.empty());

		LinearConstraint[] C = buildConstraints(normalized, nonnegative, coefficients, values, rel);

		// add constraints to this factor
		for (LinearConstraint c : C) {
			this.addConstraint(c);
		}
	}

	public SeparateHalfspaceDefaultFactor(Strides left, Strides right, double[][][] coefficients, double[][] values, Relationship rel) {
		this(left, right);

		for (int i = 0; i < right.getCombinations(); i++) {
			// Build the constraints (including normalization and non-negative one)
			LinearConstraint[] C = buildConstraints(true, true, coefficients[i], values[i], rel);
			// add constraints to this factor
			for (LinearConstraint c : C) {
				this.addConstraint(c, i);
			}
		}
	}

	public static LinearConstraint[] buildConstraints(boolean normalized, boolean nonnegative, double[][] coefficients, double[] values, Relationship... rel) {
		int left_combinations = coefficients[0].length;

		List<LinearConstraint> C = new ArrayList<>();

		// check the coefficient shape
		for (double[] c : coefficients) {
			if (c.length != left_combinations)
				throw new IllegalArgumentException("ERROR: coefficient matrix shape");
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
			throw new IllegalArgumentException("ERROR: wrong relationship vector length: " + rel.length);
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

	@Override
	public SeparateHalfspaceDefaultFactor copy() {
		List<List<LinearConstraint>> newData = new ArrayList<>(groupDomain.getCombinations());
		for (List<LinearConstraint> datum : data) {
			List<LinearConstraint> original = new ArrayList<>(datum);
			List<LinearConstraint> newMatrix = new ArrayList<>(original.size());

			for (LinearConstraint originalRow : original) {
				LinearConstraint copy = new LinearConstraint(originalRow.getCoefficients().copy(), originalRow.getRelationship(), originalRow.getValue());
				newMatrix.add(copy);
			}
			newData.add(newMatrix);
		}
		return new SeparateHalfspaceDefaultFactor(dataDomain, groupDomain, newData);
	}

/*
	public SeparateHalfspaceFactor copy() {
		ArrayList<ArrayList<LinearConstraint>> new_data = new ArrayList<>(groupDomain.getCombinations());
		for (ArrayList<LinearConstraint> original : data) {
			ArrayList<LinearConstraint> new_matrix = new ArrayList<>(original.size());
			for (LinearConstraint original_row : original) {
				LinearConstraint copy = new LinearConstraint(original_row.getCoefficients().copy(), original_row.getRelationship(), original_row.getValue());
				new_matrix.add(copy);
			}
			new_data.add(new_matrix);
		}
		return new SeparateHalfspaceFactor(dataDomain, groupDomain, new_data);
	}
*/

	public void addConstraint(double[] data, Relationship rel, double value, int... states) {
		this.addConstraint(new LinearConstraint(data, rel, value), states);
	}

	public void addConstraint(LinearConstraint c, int... states) {
		int offset = groupDomain.getOffset(states);
		this.data.get(offset).add(c);
	}

	public double[] getRandomVertex(int... states) {
		Random random = RandomUtil.getRandom();
		int offset = groupDomain.getOffset(states);
		SimplexSolver solver = new SimplexSolver();

		double[] coeffs = new double[dataDomain.getCombinations()];
		for (int i = 0; i < dataDomain.getCombinations(); ++i) {
			coeffs[i] = random.nextDouble() + 1;
		}

		LinearObjectiveFunction c = new LinearObjectiveFunction(coeffs, 0);
		LinearConstraintSet Ab = new LinearConstraintSet(data.get(offset));

		PointValuePair pvp = solver.optimize(Ab, c);
		return pvp.getPointRef();
	}

	/**
	 * Filter the factor by setting a specific variable to a state. This only works when the
	 * var is in the grouping/separated part.
	 *
	 * <p>Note that the constraits sets are not copied. So changing this factor will update
	 * also the filtered one.</p>
	 *
	 * @param variable
	 * @param state
	 * @return
	 */
	@Override
	public SeparateHalfspaceDefaultFactor filter(int variable, int state) {
		int var_offset = groupDomain.indexOf(variable);

		List<List<LinearConstraint>> newConstraints = new ArrayList<>();
		Strides newDataDomain = dataDomain, newGroupDomain = groupDomain;

		// todo: consider case with more than one variable on the left

		if (dataDomain.contains(variable)) {
			for (int i = 0; i < groupDomain.getCombinations(); i++) {
				Collection<LinearConstraint> constraints = SeparateHalfspaceDefaultFactor.deterministic(dataDomain, state).getLinearProblem().getConstraints();
				newConstraints.add(new ArrayList<>(constraints));
			}

		} else {
			IndexIterator it = this.getSeparatingDomain().getFiteredIndexIterator(new int[]{variable}, new int[]{state});
			while (it.hasNext())
				newConstraints.add(data.get(it.next()));

			newGroupDomain = groupDomain.removeAt(var_offset);
		}

		return new SeparateHalfspaceDefaultFactor(newDataDomain, newGroupDomain, newConstraints);
	}

/*	@Override
	public SeparateHalfspaceFactor filter(int variable, int state) {
		int var_offset = groupDomain.indexOf(variable);
		int var_stride = groupDomain.getStrideAt(var_offset);
		int next_stride = groupDomain.getStrideAt(var_offset + 1);
		
		int state_offset = var_stride * state;
		int block_count = next_stride / var_stride;
		
		Strides new_domain = groupDomain.removeAt(var_offset);
		int new_size = new_domain.getCombinations();
		ArrayList<ArrayList<LinearConstraint>> new_constraints = new ArrayList<>(new_size);
		
		for (int i = 0; i < block_count; ++i) {
			int offset = i * next_stride + state_offset;
			new_constraints.addAll(data.subList(offset, offset + var_stride));
		}
		
		return new SeparateHalfspaceFactor(dataDomain, new_domain, new_constraints);
	}*/

	@Override
	public LinearConstraintSet getLinearProblem(int... states) {
		int offset = groupDomain.getOffset(states);
		return new LinearConstraintSet(data.get(offset));
	}

	public void printLinearProblem(int... states) {
		if (states.length > 0) {
			for (LinearConstraint c : this.getLinearProblem(states).getConstraints()) {
				System.out.println(c.getCoefficients() + "\t" + c.getRelationship() + "\t" + c.getValue());
			}
		} else {
			for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
				this.printLinearProblem(i);
				System.out.println("-------");
			}
		}
	}

	@Override
	public LinearConstraintSet getLinearProblemAt(int offset) {
		return new LinearConstraintSet(data.get(offset));
	}

	/**
	 * Modifies the linear problem at a given offset.
	 *
	 * @param offset
	 * @param constraints
	 */
	public void setLinearProblemAt(int offset, LinearConstraintSet constraints) {
		List<LinearConstraint> list = new ArrayList<>(constraints.getConstraints());
		data.set(offset, list);
	}

	/**
	 * Modifies the linear problem at a given offset.
	 *
	 * @param offset
	 * @param constraints
	 */
	public void setLinearProblemAt(int offset, Collection<LinearConstraint> constraints) {
		data.set(offset, (List<LinearConstraint>) constraints);
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
			LinearConstraint[] C = SeparateHalfspaceDefaultFactor.buildConstraints(true, true, coeff, values, Relationship.EQ);

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
		return SeparateHalfspaceDefaultFactor.deterministic(left, Strides.empty(), assignment);
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 *
	 * @param var        int - id for the single children variable.
	 * @param assignment int - single value to assign
	 * @return
	 */
	public SeparateHalfspaceDefaultFactor getDeterministic(int var, int assignment) {
		return SeparateHalfspaceDefaultFactor.deterministic(this.getDomain().intersection(var), assignment);
	}

	public List<List<LinearConstraint>> getData() {
		return data;
	}

	public SeparateHalfspaceDefaultFactor getPerturbedZeroConstraints(double eps) {
		SeparateHalfspaceDefaultFactor newFactor = new SeparateHalfspaceDefaultFactor(this.getDataDomain(), this.getSeparatingDomain());
		for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
			newFactor.setLinearProblemAt(i, ConstraintsUtil.perturbZeroConstraints(this.getLinearProblem(i).getConstraints(), eps));
		}
		return newFactor;
	}

	public SeparateHalfspaceDefaultFactor removeNormConstraints() {
		SeparateHalfspaceDefaultFactor newFactor = new SeparateHalfspaceDefaultFactor(this.getDataDomain(), this.getSeparatingDomain());
		for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
			newFactor.setLinearProblemAt(i,
					ConstraintsUtil.removeNormalization(this.getLinearProblemAt(i).getConstraints())
			);
		}
		return newFactor;
	}

	public SeparateHalfspaceDefaultFactor removeNonNegativeConstraints() {
		SeparateHalfspaceDefaultFactor newFactor = new SeparateHalfspaceDefaultFactor(this.getDataDomain(), this.getSeparatingDomain());
		for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
			newFactor.setLinearProblemAt(i,
					ConstraintsUtil.removeNonNegative(this.getLinearProblemAt(i).getConstraints())
			);
		}
		return newFactor;
	}

	public SeparateHalfspaceDefaultFactor mergeCompatible() {
		SeparateHalfspaceDefaultFactor newFactor = new SeparateHalfspaceDefaultFactor(this.getDataDomain(), this.getSeparatingDomain());
		for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
			newFactor.setLinearProblemAt(i,
					ConstraintsUtil.mergeCompatible(this.getLinearProblemAt(i).getConstraints())
			);
		}
		return newFactor;
	}

	/**
	 * Retruns all the constraints
	 *
	 * @param data
	 */
	public void setData(List<List<LinearConstraint>> data) {
		this.data = data;
	}

	/**
	 * Sorts the parents following the global variable order
	 *
	 * @return
	 */
	public SeparateHalfspaceDefaultFactor sortParents() {
		Strides oldLeft = getSeparatingDomain();
		Strides newLeft = oldLeft.sort();
		int parentComb = this.getSeparatingDomain().getCombinations();
		IndexIterator it = oldLeft.getReorderedIterator(newLeft.getVariables());
		int j;

		SeparateHalfspaceDefaultFactor newFactor = new SeparateHalfspaceDefaultFactor(getDataDomain(), newLeft);

		// i -> j
		for (int i = 0; i < parentComb; i++) {
			j = it.next();
			newFactor.setLinearProblemAt(i, getLinearProblemAt(j));
		}
		return newFactor;
	}

}