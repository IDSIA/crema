package ch.idsia.crema.factor.convert;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.junit.Test;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.ExtensiveHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.solver.commons.Simplex;

public class SeparateLinearToExtensiveHalfspace {
    @Test
    public void testFromIntervalFactor1(){
        SeparateLinearToExtensiveHalfspaceFactor converter = new SeparateLinearToExtensiveHalfspaceFactor();

        IntervalFactor a = new IntervalFactor(Strides.var(1, 2), Strides.var(0, 4));
        a.set(new double[]{0.11, 0.12}, new double[]{0.88, 0.89}, 0);
        a.set(new double[]{0.21, 0.22}, new double[]{0.78, 0.79}, 1);
        a.set(new double[]{0.31, 0.32}, new double[]{0.68, 0.69}, 2);
        a.set(new double[]{0.31, 0.32}, new double[]{0.68, 0.69}, 3);
        
        ExtensiveHalfspaceFactor factor = converter.apply(a);
        assertArrayEquals(new int[] {0,1}, factor.getDomain().getVariables());
        
        Simplex simplex = new Simplex();
        simplex.loadProblem(factor, GoalType.MINIMIZE);
        simplex.solve(new double[]{0.5,0.3,0.4,0.1,0.6,0.3}, 0.0);
    }

    /** different variable order */
    @Test
    public void testFromIntervalFactor2(){
        SeparateLinearToExtensiveHalfspaceFactor converter = new SeparateLinearToExtensiveHalfspaceFactor();

        IntervalFactor a = new IntervalFactor(Strides.var(1, 3), Strides.var(0, 3));
        a.set(new double[]{0.11, 0.12, 0.13}, new double[]{0.88, 0.89, 1}, 0);
        a.set(new double[]{0.21, 0.22, 0.23}, new double[]{0.78, 0.79, 1}, 1);
        a.set(new double[]{0.31, 0.32, 0.33}, new double[]{0.68, 0.69, 1}, 2);
        
        
        ExtensiveHalfspaceFactor factor = converter.apply(a);
        assertArrayEquals(new int[] {0,1}, factor.getDomain().getVariables());

        Simplex simplex = new Simplex();
        simplex.loadProblem(factor, GoalType.MINIMIZE);
        simplex.solve(new double[]{0.5,0.3,0.4,0.1,0.6,0.3}, 0.0);

    }
    
}
