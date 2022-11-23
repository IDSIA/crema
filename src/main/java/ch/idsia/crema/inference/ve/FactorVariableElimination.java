package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.algebra.FactorAlgebra;

/**
 * A specialization of the {@link VariableElimination} algorithm for factors that implement
 * the common operations (Combination, Marginalization).
 * Such factors include the Bayesian one.
 *
 * @param <F> the type of the factors
 * @author huber
 */
public class FactorVariableElimination<F extends OperableFactor<F>> extends VariableElimination<F> {

	public FactorVariableElimination(int[] seq) {
		super(seq);
	}

}
