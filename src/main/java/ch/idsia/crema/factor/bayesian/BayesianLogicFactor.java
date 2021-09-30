package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    30.09.2021 11:22
 */
public abstract class BayesianLogicFactor extends BayesianFunctionFactor {

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
	 * Flag set to true if the variable of this node is observed.
	 */
	protected boolean isObserved;
	/**
	 * If the flag {@link #isObserved} is set to true, then this field will have the assigned state.
	 */
	protected int observedState;
	/**
	 * Variables that are observed during filtering.
	 */
	protected int[] observedVariables;
	/**
	 * States of the variables that are observed
	 */
	protected int[] observedStates;


	/**
	 * A logic factor where the true states for each parent are defined externally.
	 *
	 * @param domain     working domain of this factor
	 * @param parents    variables that are parents of this factor
	 * @param trueStates index of the state to be considered as TRUE for each given parent
	 */
	public BayesianLogicFactor(Strides domain, int[] parents, int[] trueStates) {
		super(domain);
		setF(this::f);
		this.variable = ArraysUtil.difference(domain.getVariables(), parents)[0];
		this.parents = parents;
		this.trueStates = trueStates;

		this.isObserved = false;
		this.observedVariables = new int[0];
		this.observedStates = new int[0];
	}

	/**
	 * A logic factor where the true state for each parent is the higher state available.
	 *
	 * @param domain  working domain of this factor
	 * @param parents variables that are parents of this factor
	 */
	public BayesianLogicFactor(Strides domain, int... parents) {
		this(domain, parents, IntStream.of(parents).map(p -> domain.getCardinality(p) - 1).toArray());
	}

	/**
	 * A logic factor where one variable has been filtered out (can be used during filter).
	 *
	 * @param factor   the original factor
	 * @param variable the variable to remove
	 * @param state    the state of the variable to remove
	 */
	protected BayesianLogicFactor(BayesianLogicFactor factor, int variable, int state) {
		super(factor.getDomain().remove(variable));
		setF(this::f);
		this.variable = factor.variable;
		this.parents = ArrayUtils.clone(factor.parents);
		this.trueStates = ArrayUtils.clone(factor.trueStates);

		// update internal observed variables state
		this.isObserved = variable == this.variable;

		if (this.isObserved) {
			// variable is the node itself
			this.observedState = state;

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
	 * Copy constructor.
	 *
	 * @param factor factor to copy from
	 */
	protected BayesianLogicFactor(BayesianLogicFactor factor) {
		super(factor.getDomain());
		setF(this::f);
		this.variable = factor.variable;
		this.parents = ArrayUtils.clone(factor.parents);
		this.trueStates = ArrayUtils.clone(factor.trueStates);

		this.isObserved = factor.isObserved;
		this.observedState = factor.observedState;
		this.observedVariables = ArrayUtils.clone(factor.observedVariables);
		this.observedStates = ArrayUtils.clone(factor.observedStates);
	}

	/**
	 * The function that implements the logic of this factor.
	 *
	 * @param i the offset of the CPT already computed by the external algorithm
	 * @return the value associated with the given offset
	 */
	abstract double f(int i);

}
