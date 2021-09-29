package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.08.2021 15:33
 */
public class BayesianNoisyOrFactor extends BayesianFunctionFactor {

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
	 * Noise activators.
	 */
	protected double[] inhibitors;

	/**
	 * Flag set to true if the variable of this node is observed.
	 */
	protected boolean isObserved;
	/**
	 * If the flag {@link #isObserved} is set to true, then this field will have the assigned state.
	 */
	protected int isObservedState;
	/**
	 * Variables that are observed during filtering.
	 */
	protected int[] observedVariables;
	/**
	 * States of the variables that are observed
	 */
	protected int[] observedStates;

	private BayesianNoisyOrFactor(BayesianNoisyOrFactor factor) {
		super(factor.getDomain());
		setF(this::f);
		this.variable = factor.variable;
		this.parents = ArrayUtils.clone(factor.parents);
		this.trueStates = ArrayUtils.clone(factor.trueStates);
		this.inhibitors = ArrayUtils.clone(factor.inhibitors);

		this.isObserved = factor.isObserved;
		this.isObservedState = factor.isObservedState;
		this.observedVariables = ArrayUtils.clone(factor.observedVariables);
		this.observedStates = ArrayUtils.clone(factor.observedStates);
	}

	/**
	 * A Noisy OR factor where the true states for each parent are defined externally.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param trueStates index of the state to be considered as TRUE for each given parent
	 * @param inhibitors value for the noise for each given parent
	 */
	public BayesianNoisyOrFactor(Strides domain, int[] parents, int[] trueStates, double[] inhibitors) {
		super(domain);
		setF(this::f);
		this.variable = ArraysUtil.difference(domain.getVariables(), parents)[0];
		this.parents = parents;
		this.trueStates = trueStates;
		this.inhibitors = inhibitors;

		this.isObserved = false;
		this.observedVariables = new int[0];
		this.observedStates = new int[0];
	}

	/**
	 * A Noisy OR factor where the true state for each parent is the higher state available.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param inhibitors value for the noise for each given parent
	 */
	public BayesianNoisyOrFactor(Strides domain, int[] parents, double[] inhibitors) {
		this(domain, parents, IntStream.of(parents).map(p -> domain.getCardinality(p) - 1).toArray(), inhibitors);
	}

	/**
	 * A Noisy OR factor that has been filtered over the given variable and its state.
	 *
	 * @param factor   original factor to filter from
	 * @param variable filtered variable
	 * @param state    state of the filtered variable
	 */
	protected BayesianNoisyOrFactor(BayesianNoisyOrFactor factor, int variable, int state) {
		super(factor.getDomain().remove(variable));
		setF(this::f);
		this.variable = factor.variable;
		this.parents = ArrayUtils.clone(factor.parents);
		this.trueStates = ArrayUtils.clone(factor.trueStates);
		this.inhibitors = ArrayUtils.clone(factor.inhibitors);

		// update internal observed variables state
		this.isObserved = variable == this.variable;

		if (this.isObserved) {
			// variable is the node itself
			this.isObservedState = state;

			this.observedVariables = ArrayUtils.clone(factor.observedVariables);
			this.observedStates = ArrayUtils.clone(factor.observedStates);
		} else {
			// variable is a parent
			this.observedVariables = new int[factor.observedVariables.length + 1];
			this.observedStates = new int[factor.observedStates.length + 1];

			System.arraycopy(factor.observedVariables, 0, this.observedVariables, 0, factor.observedVariables.length);
			System.arraycopy(factor.observedStates, 0, this.observedStates, 0, factor.observedStates.length);

			this.observedVariables[this.observedVariables.length - 1] = variable;
			this.observedStates[this.observedStates.length - 1] = state;
		}
	}

	/**
	 * @param offset offset to get the value for
	 * @return 1.0 when at least one of the parents is in the TRUE state, otherwise 0.0
	 */
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
		if (isObserved && isObservedState == 1)
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

		if (p > -1 && trueStates[p] != state && parents.length == 1)
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
