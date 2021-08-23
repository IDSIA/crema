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

		if (states[vars[variable]] == 1) {
			// P(or = y | ...)
			return 1 - Q;
		}
		// P(or = n | ...)
		return Q;
	}

	@Override
	public BayesianFactor copy() {
		return new BayesianNoisyOrFactor(domain, ArrayUtils.clone(parents), ArrayUtils.clone(trueStates), ArrayUtils.clone(inhibitors));
	}

	@Override
	public BayesianAbstractFactor filter(int variable, int state) {
		// TODO: is this still valid for noisy-or?
		final int p = ArraysUtil.indexOf(variable, parents);

		if (p > -1 && trueStates[p] == state)
			return BayesianFactorFactory.one(this.variable);

		if (p > -1 && trueStates[p] != state && parents.length == 1)
			return BayesianFactorFactory.zero(this.variable);

		return new BayesianNoisyOrFactor(
				getDomain().remove(variable),
				ArrayUtils.remove(parents, p),
				ArrayUtils.remove(trueStates, p),
				ArrayUtils.remove(inhibitors, p)
		);
	}

	@Override
	public String toString() {
		return "NoisyOR(" + Arrays.toString(domain.getVariables()) + ")";
	}
}
