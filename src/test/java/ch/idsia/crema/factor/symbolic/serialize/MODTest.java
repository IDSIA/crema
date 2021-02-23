package ch.idsia.crema.factor.symbolic.serialize;

import org.junit.Test;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;

public class MODTest {

    @Test
    public void testLinear() {
        Strides domain = Strides.var(0, 3);

        IntervalFactor iFactor = new IntervalFactor(domain, Strides.EMPTY);
        iFactor.setLower(new double[]{ 0.4, 0.2, 0.6 });
        iFactor.setUpper(new double[]{ 0.6, 0.3, 0.7 });

        SymbolicFactor A = new PriorFactor(iFactor);

        iFactor = new IntervalFactor(Strides.var(1,2), domain);
        iFactor.setLower(new double[]{ 0.4, 0.2 }, 0);
        iFactor.setUpper(new double[]{ 0.8, 0.6 }, 0);
        iFactor.setLower(new double[]{ 0.1, 0.2 }, 1);
        iFactor.setUpper(new double[]{ 0.8, 0.9 }, 1);
        iFactor.setLower(new double[]{ 0.4, 0.5 }, 2);
        iFactor.setUpper(new double[]{ 0.5, 0.6 }, 2);
        
        SymbolicFactor B_A = new PriorFactor(iFactor);
        
        SymbolicFactor BA = B_A.combine(A);
        SymbolicFactor B = BA.marginalize(0);

        String out = new MOD().serialize(B, 1, true);
        System.out.println(out);
    }
}
