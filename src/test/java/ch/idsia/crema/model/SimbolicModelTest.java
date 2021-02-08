package ch.idsia.crema.model;

import org.junit.Test;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.symbolic.CombinedFactor;
import ch.idsia.crema.factor.symbolic.MarginalizedFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;

public class SimbolicModelTest {
    @Test
    public void test(){

        IntervalFactor factorA = new IntervalFactor(Strides.var(1, 3), Strides.empty());
        PriorFactor priorA = new PriorFactor(factorA);

        
        IntervalFactor factorB = new IntervalFactor(Strides.var(0, 2), Strides.var(1, 3));
        PriorFactor priorB = new PriorFactor(factorB);

        CombinedFactor combined = priorA.combine(priorB);
        MarginalizedFactor marginal = combined.marginalize(1);
    }
}
