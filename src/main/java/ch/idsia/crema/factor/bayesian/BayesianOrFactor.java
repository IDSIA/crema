package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.08.2021 15:33
 */
public class BayesianOrFactor extends BayesianLogicFactor {

	/**
	 * An OR factor where the true states for each parent are defined externally.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param trueStates index of the state to be considered as TRUE for each given parent
	 */
	public BayesianOrFactor(Strides domain, int[] parents, int[] trueStates) {
		super(domain, parents, trueStates);
	}

	/**
	 * An OR factor where the true state for each parent is the higher state available.
	 *
	 * @param domain  working domain of this factor
	 * @param parents variables that are parents of this factor
	 */
	public BayesianOrFactor(Strides domain, int... parents) {
		super(domain, parents);
	}

	protected BayesianOrFactor(BayesianOrFactor factor, int variable, int state) {
		super(factor, variable, state);
	}

	/**
	 * Copy constructor.
	 *
	 * @param factor factor to copy from
	 */
	private BayesianOrFactor(BayesianOrFactor factor) {
		super(factor);
	}

	/**
	 * @param offset offset to get the value for
	 * @return 1.0 when at least one of the parents is in the TRUE state, otherwise 0.0
	 */
	@Override
	protected double f(int offset) {
		final int[] states = domain.getStatesFor(offset);
		final int[] vars = domain.getVariables();

		final boolean invert;
		if (isObserved) {
			invert = observedState == 1;
		} else {
			invert = states[ArraysUtil.indexOf(variable, vars)] == 1;
		}

		final double r_true = invert ? 1.0 : 0.0;
		final double r_false = invert ? 0.0 : 1.0;

		// if one of the observed variables is a parent and its state is true, return true
		for (int v : observedVariables) {
			final int x = ArraysUtil.indexOf(v, observedVariables);
			final int p = ArraysUtil.indexOf(v, parents);
			if (x > -1 && p > -1 && observedStates[x] != trueStates[p])
				return r_true;
		}

		for (int i = 0; i < vars.length; i++) {
			final int v = vars[i];
			if (ArrayUtils.contains(parents, v)) {
				if (trueStates[ArraysUtil.indexOf(v, parents)] == states[i]) {
					return r_true;
				}
			}
		}

		return r_false;
	}

	@Override
	public BayesianFactor copy() {
		return new BayesianOrFactor(this);
	}

	@Override
	public BayesianAbstractFactor filter(int variable, int state) {
		final int p = ArraysUtil.indexOf(variable, parents);

		if (p > -1 && trueStates[p] == state)
			return BayesianFactorFactory.one(this.variable);

		// check for last parent
		if (p > -1 && trueStates[p] != state && parents.length - observedVariables.length == 1)
			return BayesianFactorFactory.zero(this.variable);

		return new BayesianOrFactor(this, variable, state);
	}

	@Override
	public String toString() {
		return "OR(" + Arrays.toString(domain.getVariables()) + ")";
	}
}
