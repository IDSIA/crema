package ch.idsia.crema.factor.credal.linear.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ConstraintsUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.SimplexSolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorUtils.deterministic;

/**
 * A separately specified Credal factor that has a list of linear constrains for each
 * separation.
 *
 * @author huber
 */
public class SeparateHalfspaceDefaultFactor extends SeparateHalfspaceAbstractFactor {

	private final TIntObjectMap<List<LinearConstraint>> data;

	SeparateHalfspaceDefaultFactor(Strides content, Strides separation) {
		super(content, separation);
		data = new TIntObjectHashMap<>(separation.getCombinations());
		for (int i = 0; i < separation.getCombinations(); i++) {
			data.put(i, new ArrayList<>());
		}
	}

	public SeparateHalfspaceDefaultFactor(Strides content, Strides separation, TIntObjectMap<List<LinearConstraint>> data) {
		this(content, separation);

		for (int i = 0; i < data.size(); i++) {
			this.data.put(i, data.get(i));
		}
	}

	@Override
	public SeparateHalfspaceDefaultFactor copy() {
		// data generates a copy of the current constraints
		return new SeparateHalfspaceDefaultFactor(dataDomain, groupDomain, getData());
	}

	protected void addConstraint(LinearConstraint c, int... states) {
		int offset = groupDomain.getOffset(states);
		this.data.get(offset).add(c);
	}

	@Override
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

		TIntObjectMap<List<LinearConstraint>> newConstraints = new TIntObjectHashMap<>();
		Strides newDataDomain = dataDomain;
		Strides newGroupDomain = groupDomain;

		TIntObjectMap<List<LinearConstraint>> data = getData();


		// TODO: consider case with more than one variable on the left

		if (dataDomain.contains(variable)) {
			for (int i = 0; i < groupDomain.getCombinations(); i++) {
				Collection<LinearConstraint> constraints = deterministic(dataDomain, state).getLinearProblem().getConstraints();
				newConstraints.put(i, new ArrayList<>(constraints));
			}

		} else {
			IndexIterator it = getSeparatingDomain().getFiteredIndexIterator(new int[]{variable}, new int[]{state});
			int j = 0; // index in the new domain
			while (it.hasNext()) {
				int i = it.next();
				newConstraints.put(j, data.get(i));
				j++;
			}


			newGroupDomain = groupDomain.removeAt(var_offset);
		}

		return new SeparateHalfspaceDefaultFactor(newDataDomain, newGroupDomain, newConstraints);
	}

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
				printLinearProblem(i);
				System.out.println("-------");
			}
		}
	}

	@Override
	public LinearConstraintSet getLinearProblemAt(int offset) {
		return new LinearConstraintSet(data.get(offset));
	}

	@Override
	public TIntObjectMap<List<LinearConstraint>> getData() {
		final TIntObjectMap<List<LinearConstraint>> newData = new TIntObjectHashMap<>();

		for (int key : data.keys()) {
			newData.put(key, new ArrayList<>());

			for (LinearConstraint original : data.get(key)) {
				LinearConstraint copy = new LinearConstraint(original.getCoefficients().copy(), original.getRelationship(), original.getValue());
				newData.get(key).add(copy);
			}
		}

		return newData;
	}

	public TIntObjectMap<List<LinearConstraint>> getDataStructure() {
		final TIntObjectMap<List<LinearConstraint>> newData = new TIntObjectHashMap<>();

		for (int key : data.keys()) {
			newData.put(key, new ArrayList<>());
		}

		return newData;
	}

	@Override
	public SeparateHalfspaceDefaultFactor getPerturbedZeroConstraints(double eps) {
		final TIntObjectMap<List<LinearConstraint>> constraints = getDataStructure();

		for (int i : data.keys()) {
			constraints.get(i).addAll(ConstraintsUtil.perturbZeroConstraints(getLinearProblem(i).getConstraints(), eps));
		}

		return new SeparateHalfspaceDefaultFactor(getDataDomain(), getSeparatingDomain(), constraints);
	}

	@Override
	public SeparateHalfspaceDefaultFactor removeNormConstraints() {
		final TIntObjectMap<List<LinearConstraint>> constraints = getDataStructure();

		for (int i : data.keys()) {
			constraints.get(i).addAll(ConstraintsUtil.removeNormalization(getLinearProblemAt(i).getConstraints()));
		}
		return new SeparateHalfspaceDefaultFactor(getDataDomain(), getSeparatingDomain(), constraints);
	}

	@Override
	public SeparateHalfspaceDefaultFactor removeNonNegativeConstraints() {
		final TIntObjectMap<List<LinearConstraint>> constraints = getDataStructure();

		for (int i : data.keys()) {
			constraints.get(i).addAll(ConstraintsUtil.removeNonNegative(getLinearProblemAt(i).getConstraints()));
		}
		return new SeparateHalfspaceDefaultFactor(getDataDomain(), getSeparatingDomain(), constraints);
	}

	@Override
	public SeparateHalfspaceDefaultFactor mergeCompatible() {
		final TIntObjectMap<List<LinearConstraint>> constraints = getDataStructure();

		for (int i : data.keys()) {
			constraints.get(i).addAll(i, ConstraintsUtil.mergeCompatible(getLinearProblemAt(i).getConstraints()));
		}
		return new SeparateHalfspaceDefaultFactor(getDataDomain(), getSeparatingDomain(), constraints);
	}

	/**
	 * Sorts the parents following the global variable order
	 *
	 * @return
	 */
	public SeparateHalfspaceDefaultFactor sortParents() {
		Strides oldLeft = getSeparatingDomain();
		Strides newLeft = oldLeft.sort();
		int parentComb = getSeparatingDomain().getCombinations();
		IndexIterator it = oldLeft.getReorderedIterator(newLeft.getVariables());
		int j;

		TIntObjectMap<List<LinearConstraint>> constraints = getDataStructure();

		// i -> j
		for (int i = 0; i < parentComb; i++) {
			j = it.next();

			constraints.get(i).addAll(getLinearProblemAt(j).getConstraints());
		}

		return new SeparateHalfspaceDefaultFactor(getDataDomain(), newLeft, constraints);
	}

}