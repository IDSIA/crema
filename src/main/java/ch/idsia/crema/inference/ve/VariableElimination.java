package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.FactorUtil;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.algebra.Operation;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.inference.ve.order.OrderingStrategy;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VariableElimination<F extends OperableFactor<F>> implements InferenceJoined<GraphicalModel<F>, F> {

	private int[] sequence;

	private List<F> factors;

	private TIntIntMap evidence;

	private final Operation<F> operator;

	private boolean normalize = true;

	/**
	 * Constructs a variable elimination specifying the algebra.
	 * Factors, evidence and elimnation sequence must be specified with setters.
	 *
	 * @param ops algebra to use
	 */
	public VariableElimination(Operation<F> ops) {
		this.operator = ops;
	}

	/**
	 * Constructs a variable elimination specifying the algebra to be used for the
	 * factors and the elimination order
	 *
	 * @param ops      algebra to use
	 * @param sequence the elimination sequence to use
	 */
	public VariableElimination(Operation<F> ops, int[] sequence) {
		setSequence(sequence);
		this.operator = ops;
	}

	/**
	 * Set the elimination sequence to be used. Variables will be eliminated in this order.
	 * The sequence may include the query!
	 * <p>Elimination sequences can be generated with an {@link OrderingStrategy}.
	 * </p>
	 *
	 * @param sequence the elimination sequence to use
	 */
	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}

	/**
	 * Populate the problem with the factors to be considered.
	 * Collection version.
	 *
	 * @param factors a collection of factors
	 */
	public void setFactors(Collection<? extends F> factors) {
		this.factors = new ArrayList<>(factors);
	}

	/**
	 * Populate the problem with the factors to be considered.
	 * Array version.
	 *
	 * @param factors an array of factors
	 */
	public void setFactors(F[] factors) {
		this.factors = Arrays.asList(factors);
	}

	/**
	 * Fix some evidence. The provided argument is a map of variable - state
	 * associations.
	 *
	 * @param evidence the observed variable as a map of variable-states
	 */
	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	/**
	 * Specify if the resulting value should be normalized.
	 * Will result in asking K(Q|e) vs K(Qe)
	 *
	 * @param norm a boolean
	 */
	public void setNormalize(boolean norm) {
		normalize = norm;
	}

	/**
	 * Execute the variable elimination asking for the marginal or posterior of the specified
	 * variables. If multiple variables are specified the joint over the query is computed.
	 * <p>
	 * <p>
	 * The elimination sequence is to be specified via {@link VariableElimination#setSequence(int[])}.
	 *
	 * @param query variables to use as query
	 * @return the joint marginal or posterior probability of the queried variables
	 */
	public F run(int... query) {
		// variables should be sorted
		query = ArraysUtil.sort(query);

		FactorQueue<F> queue = new FactorQueue<>(sequence);
		queue.addAll(factors);
		boolean normalize = false;
		F last = null;
		while (queue.hasNext()) {
			int variable = queue.getVariable();
			Collection<F> var_factors = queue.next();

			if (var_factors.size() > 0) {
				last = FactorUtil.combine(operator, var_factors);

				if (Arrays.binarySearch(query, variable) >= 0) {
					// query var
					// nothing to do
				} else if (evidence != null && evidence.containsKey(variable)) {
					int state = evidence.get(variable);
					last = operator.filter(last, variable, state);
					normalize = true;
				} else {
					last = operator.marginalize(last, variable);
				}
				queue.add(last);
			}
		}

		if (normalize && this.normalize) {
			last = FactorUtil.normalize(operator, last);
		}

		return last;
	}

	@Override
	public F query(GraphicalModel<F> model, TIntIntMap evidence, int query) {
		return query(model, evidence, new int[]{query});
	}

	@Override
	public F query(GraphicalModel<F> model, TIntIntMap observations, int... queries) {
		setEvidence(observations);
		setFactors(model.getFactors());
		return run(queries);
	}

}
