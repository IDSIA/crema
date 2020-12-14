package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.model.math.FactorOperation;

/**
 * A specialization of the {@link VariableElimination} algorithm for factors that implement
 * the common operations (Combination, Marginalization).
 * Such factors include the Bayesian one.
 *
 * @param <F> the type of the factors
 * @author huber
 */
public class FactorVariableElimination<F extends Factor<F>> extends VariableElimination<F> {

	public FactorVariableElimination(int[] seq) {
		super(new FactorOperation<>(), seq);
	}

}
