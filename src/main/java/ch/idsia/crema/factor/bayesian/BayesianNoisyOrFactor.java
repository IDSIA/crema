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
public class BayesianNoisyOrFactor extends BayesianLogicFactor {

	/**
	 * Noise activators.
	 */
	protected double[] inhibitors;


	/**
	 * A Noisy OR factor where the true states for each parent are defined externally.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param trueStates index of the state to be considered as TRUE for each given parent
	 * @param inhibitors value for the noise for each given parent
	 */
	public BayesianNoisyOrFactor(Strides domain, int[] parents, int[] trueStates, double[] inhibitors) {
		super(domain, parents, trueStates);
		setF(this::f);
		this.inhibitors = inhibitors;
	}

	/**
	 * A Noisy OR factor where the true state for each parent is the higher state available.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param inhibitors value for the noise for each given parent
	 */
	public BayesianNoisyOrFactor(Strides domain, int[] parents, double[] inhibitors) {
		super(domain, parents);
		this.inhibitors = inhibitors;
	}

	/**
	 * Copy constructor.
	 *
	 * @param factor factor to copy from
	 */
	private BayesianNoisyOrFactor(BayesianNoisyOrFactor factor) {
		super(factor);
		this.inhibitors = ArrayUtils.clone(factor.inhibitors);
	}

	/**
	 * A Noisy OR factor that has been filtered over the given variable and its state.
	 *
	 * @param factor   original factor to filter from
	 * @param variable filtered variable
	 * @param state    state of the filtered variable
	 */
	protected BayesianNoisyOrFactor(BayesianNoisyOrFactor factor, int variable, int state) {
		super(factor, variable, state);
		this.inhibitors = ArrayUtils.clone(factor.inhibitors);
	}

	/**
	 * @param offset offset to get the value for
	 * @return applies the inhibitions to compute the value
	 */
	@Override
	protected double f(int offset) {
		final int[] states = domain.getStatesFor(offset);
		final int[] vars = domain.getVariables();

		// Q = q1 * q2 * ... * qn where qi = inhibitor value if Pi = true
		double Q = 1.0;
		for (int i = 0; i < vars.length; i++) {
			final int v = vars[i];
			final int p = ArraysUtil.indexOf(v, parents);
			if (p > -1) {
				if (trueStates[p] == states[i]) {
					Q *= inhibitors[p];
				}
			}
		}

		// inhibitor value of observed parents
		for (int v : observedVariables) {
			final int x = ArraysUtil.indexOf(v, observedVariables);
			final int p = ArraysUtil.indexOf(v, parents);
			if (x > -1 && p > -1 && observedStates[x] == trueStates[p]) {
				Q *= inhibitors[p];
			}
		}

		// output if the node has evidence
		if (isObserved && observedState == 1)
			// P(or = y | ...)
			return 1 - Q;
		if (isObserved)
			// P(or = n | ...)
			return Q;

		// output if the node has no evidence
		if (states[ArraysUtil.indexOf(variable, vars)] == 1)
			// P(or = y | ...)
			return 1 - Q;

		// P(or = n | ...)
		return Q;
	}

	@Override
	public BayesianFactor copy() {
		return new BayesianNoisyOrFactor(this);
	}

	@Override
	public BayesianAbstractFactor filter(int variable, int state) {
		final int p = ArraysUtil.indexOf(variable, parents);

		if (p > -1 && trueStates[p] != state && parents.length - observedVariables.length == 1)
			// last parents' state is off
			return BayesianFactorFactory.zero(this.variable);

		// parent or variable is observed
		return new BayesianNoisyOrFactor(this, variable, state);
	}

	@Override
	public String toString() {
		return "NoisyOR(" + Arrays.toString(domain.getVariables()) + ")";
	}
}
