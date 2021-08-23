package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    23.08.2021 14:40
 */
public class BayesianAndFactor extends BayesianFunctionFactor {

	/**
	 * Variables defined on this factor (not parents).
	 */
	protected int variable;
	/**
	 * Variables that are parents.
	 */
	protected int[] parents;
	/**
	 * Value of the TRUE state for each parent.
	 */
	protected int[] trueStates;

	/**
	 * An OR factor where the true states for each parent are defined externally.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param trueStates index of the state to be considered as TRUE for each given parent
	 */
	public BayesianAndFactor(Strides domain, int[] parents, int[] trueStates) {
		super(domain);
		setF(this::f);
		this.variable = ArraysUtil.difference(domain.getVariables(), parents)[0];
		this.parents = parents;
		this.trueStates = trueStates;
	}

	/**
	 * An OR factor where the true state for each parent is the higher state available.
	 *
	 * @param domain  working domain of this factor
	 * @param parents variables that are parents of this factor
	 */
	public BayesianAndFactor(Strides domain, int... parents) {
		this(domain, parents, IntStream.of(parents).map(p -> domain.getCardinality(p) - 1).toArray());
	}

	/**
	 * @param offset offset to get the value for
	 * @return 0.0 when at least one of the parents is in the FALSE state, otherwise 1.0
	 */
	protected double f(int offset) {
		final int[] states = domain.getStatesFor(offset);
		final int[] vars = domain.getVariables();

		final boolean invert = states[vars[variable]] == 1;
		final double r_false = invert ? 0.0 : 1.0;
		final double r_true = invert ? 1.0 : 0.0;

		for (int i = 0; i < vars.length; i++) {
			final int v = vars[i];
			if (ArrayUtils.contains(parents, v)) {
				if (trueStates[ArraysUtil.indexOf(v, parents)] != states[i]) {
					return r_false;
				}
			}
		}

		return r_true;
	}

	@Override
	public BayesianFactor copy() {
		return new BayesianAndFactor(domain, ArrayUtils.clone(parents), ArrayUtils.clone(trueStates));
	}

	@Override
	public BayesianAbstractFactor filter(int variable, int state) {
		final int p = ArraysUtil.indexOf(variable, parents);

		if (p > -1 && trueStates[p] != state)
			return BayesianFactorFactory.zero(this.variable);

		if (p > -1 && trueStates[p] == state && parents.length == 1)
			return BayesianFactorFactory.one(this.variable);

		return new BayesianAndFactor(getDomain().remove(variable), ArrayUtils.remove(parents, p), ArrayUtils.remove(trueStates, p));
	}

	@Override
	public String toString() {
		return "AND(" + Arrays.toString(domain.getVariables()) + ")";
	}
}
