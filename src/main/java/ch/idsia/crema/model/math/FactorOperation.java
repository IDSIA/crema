package ch.idsia.crema.model.math;

import ch.idsia.crema.factor.OperableFactor;

public class FactorOperation<F extends OperableFactor<F>> implements Operation<F> {

	@Override
	public F combine(F f1, F f2) {
		return f1.combine(f2);
	}

	@Override
	public F marginalize(F one, int variable) {
		return one.marginalize(variable);
	}

	@Override
	public F divide(F one, F other) {
		return one.divide(other);
	}

	@Override
	public F filter(F one, int variable, int state) {
		return one.filter(variable, state);
	}

}
