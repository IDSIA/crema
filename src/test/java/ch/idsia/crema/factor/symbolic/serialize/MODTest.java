package ch.idsia.crema.factor.symbolic.serialize;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import org.junit.jupiter.api.Test;

public class MODTest {

	@Test
	public void testLinear() {
		Strides domain = Strides.var(1, 2);

		IntervalFactor iFactor = IntervalFactorFactory.factory().domain(domain, Strides.EMPTY)
				.lower(new double[]{0.4, 0.1})
				.upper(new double[]{0.9, 0.6})
				.get();

		SymbolicFactor A = new PriorFactor(iFactor);

		iFactor = IntervalFactorFactory.factory().domain(Strides.var(0, 3), domain)
				.lower(new double[]{0.4, 0.2, 0.1}, 0)
				.upper(new double[]{0.8, 0.6, 0.5}, 0)
				.lower(new double[]{0.1, 0.2, 0.6}, 1)
				.upper(new double[]{0.3, 0.5, 0.8}, 1)
				.get();

		SymbolicFactor B_A = new PriorFactor(iFactor);

		SymbolicFactor BA = B_A.combine(A);
		SymbolicFactor B = BA.marginalize(1);

		String out = new MOD().serialize(B, 0, true);
	}
}
