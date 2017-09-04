package ch.idsia.crema.factor;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.DomainBuilder;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.user.credal.Interval;
import org.junit.Test;
import static org.junit.Assert.*;

public class IntervalFactorTest {

  @Test
  public void testFilter(){
    Strides parents_domain = DomainBuilder.var(0,1).size(3,3).strides();
    Strides domain =Strides.var(2,3);

    IntervalFactor ifact = new IntervalFactor(domain, parents_domain);
    ifact.set(new double[] { .1,0,0}, new double[] {.2,0,0}, 0,0);
    ifact.set(new double[] { 0,.1,0}, new double[] {0,.2,0}, 1,0);
    ifact.set(new double[] { 0,0,0.1}, new double[] {0,0,0.2}, 2,0);

    ifact.set(new double[] { .3,0,0}, new double[] {.4,0,0}, 0,1);
    ifact.set(new double[] { 0,.3,0}, new double[] {0,.4,0}, 1,1);
    ifact.set(new double[] { 0,0,0.3}, new double[] {0,0,0.4}, 2,1);

    ifact.set(new double[] { .5,0,0}, new double[] {.6,0,0}, 0,2);
    ifact.set(new double[] { 0,.5,0}, new double[] {0,.6,0}, 1,2);
    ifact.set(new double[] { 0,0,0.5}, new double[] {0,0,0.6}, 2,2);

    IntervalFactor if2 = ifact.filter(0,1);
    assertArrayEquals(if2.getLower(0), new double[]{0, 0.1, 0}, 0.000001);
    assertArrayEquals(if2.getLower(1), new double[]{0, 0.3, 0}, 0.000001);
    assertArrayEquals(if2.getLower(2), new double[]{0, 0.5, 0}, 0.000001);

    assertArrayEquals(if2.getUpper(0), new double[]{0, 0.2, 0}, 0.000001);
    assertArrayEquals(if2.getUpper(1), new double[]{0, 0.4, 0}, 0.000001);
    assertArrayEquals(if2.getUpper(2), new double[]{0, 0.6, 0}, 0.000001);


    if2 = ifact.filter(1,1);

    assertArrayEquals(if2.getLower(0), new double[]{0.3, 0, 0}, 0.000001);
    assertArrayEquals(if2.getLower(1), new double[]{0, 0.3, 0}, 0.000001);
    assertArrayEquals(if2.getLower(2), new double[]{0, 0, 0.3}, 0.000001);

    assertArrayEquals(if2.getUpper(0), new double[]{0.4, 0, 0}, 0.000001);
    assertArrayEquals(if2.getUpper(1), new double[]{0, 0.4, 0}, 0.000001);
    assertArrayEquals(if2.getUpper(2), new double[]{0, 0, 0.4}, 0.000001);


  }
}
