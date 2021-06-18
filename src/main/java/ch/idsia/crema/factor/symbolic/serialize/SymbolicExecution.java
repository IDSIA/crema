package ch.idsia.crema.factor.symbolic.serialize;

import ch.idsia.crema.factor.FactorUtil;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.algebra.Operation;
import ch.idsia.crema.factor.symbolic.*;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    28.04.2021 17:19
 */
// TODO this is just an example and need much more work to be usable!
public class SymbolicExecution<F extends OperableFactor<F>> {

	private final Operation<F> algebra;

	public SymbolicExecution(Operation<F> algebra) {
		this.algebra = algebra;
	}

	@SuppressWarnings("unchecked")
	public F exec(SymbolicFactor factor) {
		F res = null;

		if (factor instanceof FilteredFactor) {
			final FilteredFactor f = (FilteredFactor) factor;
			res = FactorUtil.filter(algebra, exec(f.getFactor()), f.getVariable(), f.getState());

		} else if (factor instanceof CombinedFactor) {
			final CombinedFactor f = (CombinedFactor) factor;
			res = FactorUtil.combine(algebra, exec(f.getFactor1()), exec(f.getFactor2()));

		} else if (factor instanceof MarginalizedFactor) {
			final MarginalizedFactor f = (MarginalizedFactor) factor;
			res = FactorUtil.marginalize(algebra, exec(f.getFactor()), f.getVariable());

		} else if (factor instanceof NormalizedFactor) {
			final NormalizedFactor f = (NormalizedFactor) factor;
			res = FactorUtil.normalize(algebra, exec(f.getFactor()), f.getGiven());

		} else if (factor instanceof DividedFactor) {
			final DividedFactor f = (DividedFactor) factor;
			res = FactorUtil.divide(algebra, exec(f.getNumerator()), exec(f.getDenominator()));

		} else if (factor instanceof PriorFactor) {
			final PriorFactor f = (PriorFactor) factor;
			res = (F) f.getFactor();

		}

		return res;
	}

}
