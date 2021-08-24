package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    23.08.2021 14:40
 */
public class BayesianNotFactor extends BayesianFunctionFactor {

	/**
	 * Variables defined on this factor (not parents).
	 */
	protected int variable;
	/**
	 * Variables that are parents.
	 */
	protected int parent;
	/**
	 * Value of the TRUE state for each parent.
	 */
	protected int trueState;

	protected BayesianNotFactor(Strides domain, int parent, int trueState) {
		super(domain);
		if (domain.getVariables().length > 2)
			throw new IllegalArgumentException("A BayesianNotFactor support only one parent!");
		setF(this::f);
		this.variable = ArraysUtil.difference(domain.getVariables(), new int[]{parent})[0];
		this.parent = parent;
		this.trueState = trueState;
	}

	public BayesianNotFactor(Strides domain, int parent) {
		this(domain, parent, domain.getCardinality(parent) - 1);
	}

	protected double f(int offset) {
		final int[] states = domain.getStatesFor(offset);
		final int[] vars = domain.getVariables();

		final int parentState = states[ArraysUtil.indexOf(parent, vars)];
		final int thisState = states[ArraysUtil.indexOf(variable, vars)];

		if (parentState == trueState)
			return thisState == 0 ? 1.0 : 0.0;

		return thisState == 0 ? 0.0 : 1.0;
	}

	@Override
	public BayesianFactor copy() {
		return new BayesianNotFactor(getDomain(), parent, trueState);
	}

	@Override
	public BayesianAbstractFactor filter(int variable, int state) {
		if (variable == parent && state == trueState)
			return BayesianFactorFactory.zero(this.variable);

		return BayesianFactorFactory.one(this.variable);
	}

	@Override
	public String toString() {
		return "NOT(" + Arrays.toString(domain.getVariables()) + ")";
	}
}
