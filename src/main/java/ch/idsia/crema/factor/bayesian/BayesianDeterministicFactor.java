package ch.idsia.crema.factor.bayesian;


import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.operations.vertex.Filter;
import ch.idsia.crema.factor.operations.vertex.LogMarginal;
import ch.idsia.crema.utility.IndexIterator;
import com.google.common.primitives.Ints;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    15.04.2021 18:51
 */
public class BayesianDeterministicFactor extends BayesianDefaultFactor {

	/**
	 * This is used internally by the clone method.
	 *
	 * @param stride
	 * @param data
	 */
	private BayesianDeterministicFactor(Strides stride, double[] data) {
		super(stride, data);
	}

	/**
	 * Constructs a deterministic factor (values can only be ones or zeros).
	 * Thus, children variables are determined by the values of the parents
	 *
	 * @param left        Strides - children variables.
	 * @param right       Strides - parent variables
	 * @param assignments assignments of each combination of the parent
	 */
	public BayesianDeterministicFactor(Strides left, Strides right, int... assignments) {
		super(left.concat(right), null);
		final int combinations = right.getCombinations();
		if (assignments.length != combinations)
			throw new IllegalArgumentException("Length of assignments should be equal to the number of combinations of the parents");

		if (Ints.min(assignments) < 0 || Ints.max(assignments) >= left.getCombinations())
			throw new IllegalArgumentException("Assignments of deterministic function should be in the interval [0," + left.getCombinations() + ")");

		data = new double[combinations];
		for (int i = 0; i < combinations; i++) {
			data[i * left.getCombinations() + assignments[i]] = 1.0;
		}
	}

	public static BayesianDefaultFactor getJoinDeterministic(Strides left_vars, TIntIntMap obs) {
		final int[] compatibleIndexes = left_vars.getCompatibleIndexes(obs);
		final double[] data = new double[compatibleIndexes.length];

		for (int index : compatibleIndexes)
			data[index] = 1;

		return new BayesianDeterministicFactor(left_vars, data);
	}

	/**
	 * Constructs a deterministic factor (values can only be ones or zeros) without parent variables.
	 *
	 * @param left       Strides - children variables.
	 * @param assignment int - single value to assign
	 */
	public BayesianDeterministicFactor(Strides left, int assignment) {
		this(left, Strides.empty(), assignment);
	}

	@Override
	public BayesianDeterministicFactor copy() {
		return new BayesianDeterministicFactor(domain, data.clone());
	}

	@Override
	public BayesianDeterministicFactor filter(int variable, int state) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Filter(domain.getStrideAt(offset), state), BayesianDeterministicFactor::new);
	}

	@Override
	public BayesianDeterministicFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (offset == -1) return this;
		return collect(offset, new LogMarginal(domain.getSizeAt(offset), domain.getStrideAt(offset)), BayesianDeterministicFactor::new);
	}

	@Override
	public BayesianDeterministicFactor combineIterator(BayesianDefaultFactor cpt) {
		Strides target = domain.union(cpt.domain);

		IndexIterator i1 = getDomain().getIterator(target);
		IndexIterator i2 = cpt.getDomain().getIterator(target);

		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			result[i] = data[i1.next()] + cpt.data[i2.next()];
		}

		return new BayesianDeterministicFactor(target, result);
	}

	@Override
	public BayesianDeterministicFactor replace(double value, double replacement) {
		BayesianDeterministicFactor f = this.copy();
		f.replaceInplace(value, replacement);
		return f;
	}

	@Override
	public BayesianDeterministicFactor replaceNaN(double replacement) {
		BayesianDeterministicFactor f = this.copy();
		for (int i = 0; i < f.data.length; i++)
			if (Double.isNaN(f.data[i]))
				setValueAt(replacement, i);
		return f;
	}

}
