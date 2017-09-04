package ch.idsia.crema.factor.linear;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.DomainBuilder;
import ch.idsia.crema.model.Strides;
import org.junit.Test;
import static org.junit.Assert.*;

public class IntervalFactorTest {

	@Test
	public void testFirstLastFilter() {
		Strides parents_domain = DomainBuilder.var(0, 1).size(3, 3).strides();
		Strides domain = Strides.var(2, 3);

		IntervalFactor ifact = new IntervalFactor(domain, parents_domain);
		ifact.set(new double[] { .1, 0, 0 }, new double[] { .2, 0, 0 }, 0, 0);
		ifact.set(new double[] { 0, .1, 0 }, new double[] { 0, .2, 0 }, 1, 0);
		ifact.set(new double[] { 0, 0, 0.1 }, new double[] { 0, 0, 0.2 }, 2, 0);

		ifact.set(new double[] { .3, 0, 0 }, new double[] { .4, 0, 0 }, 0, 1);
		ifact.set(new double[] { 0, .3, 0 }, new double[] { 0, .4, 0 }, 1, 1);
		ifact.set(new double[] { 0, 0, 0.3 }, new double[] { 0, 0, 0.4 }, 2, 1);

		ifact.set(new double[] { .5, 0, 0 }, new double[] { .6, 0, 0 }, 0, 2);
		ifact.set(new double[] { 0, .5, 0 }, new double[] { 0, .6, 0 }, 1, 2);
		ifact.set(new double[] { 0, 0, 0.5 }, new double[] { 0, 0, 0.6 }, 2, 2);

		IntervalFactor if2 = ifact.filter(0, 1);
		assertArrayEquals(if2.getLower(0), new double[] { 0, 0.1, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(1), new double[] { 0, 0.3, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(2), new double[] { 0, 0.5, 0 }, 0.000001);

		assertArrayEquals(if2.getUpper(0), new double[] { 0, 0.2, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(1), new double[] { 0, 0.4, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(2), new double[] { 0, 0.6, 0 }, 0.000001);

		if2 = ifact.filter(1, 1);

		assertArrayEquals(if2.getLower(0), new double[] { 0.3, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(1), new double[] { 0, 0.3, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(2), new double[] { 0, 0, 0.3 }, 0.000001);

		assertArrayEquals(if2.getUpper(0), new double[] { 0.4, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(1), new double[] { 0, 0.4, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(2), new double[] { 0, 0, 0.4 }, 0.000001);

	}

	@Test
	public void testMidFilter() {
		Strides parents_domain = DomainBuilder.var(0, 1, 4).size(3, 2, 3).strides();
		Strides domain = Strides.var(2, 3);

		IntervalFactor ifact = new IntervalFactor(domain, parents_domain);
		ifact.set(new double[] { .1, 0, 0 }, new double[] { .15, 0, 0 }, 0, 0, 0);
		ifact.set(new double[] { 0, .1, 0 }, new double[] { 0, .15, 0 }, 1, 0, 0);
		ifact.set(new double[] { 0, 0, .1 }, new double[] { 0, 0, .15 }, 2, 0, 0);

		ifact.set(new double[] { .2, 0, 0 }, new double[] { .25, 0, 0 }, 0, 1, 0);
		ifact.set(new double[] { 0, .2, 0 }, new double[] { 0, .25, 0 }, 1, 1, 0);
		ifact.set(new double[] { 0, 0, .2 }, new double[] { 0, 0, .25 }, 2, 1, 0);

		ifact.set(new double[] { .3, 0, 0 }, new double[] { .35, 0, 0 }, 0, 0, 1);
		ifact.set(new double[] { 0, .3, 0 }, new double[] { 0, .35, 0 }, 1, 0, 1);
		ifact.set(new double[] { 0, 0, .3 }, new double[] { 0, 0, .35 }, 2, 0, 1);

		ifact.set(new double[] { .4, 0, 0 }, new double[] { .45, 0, 0 }, 0, 1, 1);
		ifact.set(new double[] { 0, .4, 0 }, new double[] { 0, .45, 0 }, 1, 1, 1);
		ifact.set(new double[] { 0, 0, .4 }, new double[] { 0, 0, .45 }, 2, 1, 1);

		ifact.set(new double[] { .5, 0, 0 }, new double[] { .55, 0, 0 }, 0, 0, 2);
		ifact.set(new double[] { 0, .5, 0 }, new double[] { 0, .55, 0 }, 1, 0, 2);
		ifact.set(new double[] { 0, 0, .5 }, new double[] { 0, 0, .55 }, 2, 0, 2);

		ifact.set(new double[] { .6, 0, 0 }, new double[] { .65, 0, 0 }, 0, 1, 2);
		ifact.set(new double[] { 0, .6, 0 }, new double[] { 0, .65, 0 }, 1, 1, 2);
		ifact.set(new double[] { 0, 0, .6 }, new double[] { 0, 0, .65 }, 2, 1, 2);

		// mid remove
		IntervalFactor if2 = ifact.filter(1, 1);
		assertArrayEquals(if2.getLower(0, 2), new double[] { .6, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(1, 2), new double[] { 0, .6, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(2, 2), new double[] { 0, 0, .6 }, 0.000001);

		assertArrayEquals(if2.getUpper(0, 2), new double[] { .65, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(1, 2), new double[] { 0, .65, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(2, 2), new double[] { 0, 0, .65 }, 0.000001);

		if2 = ifact.filter(1, 0);

		assertArrayEquals(if2.getLower(0, 2), new double[] { .5, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(1, 2), new double[] { 0, .5, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(2, 2), new double[] { 0, 0, .5 }, 0.000001);

		assertArrayEquals(if2.getUpper(0, 2), new double[] { .55, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(1, 2), new double[] { 0, .55, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(2, 2), new double[] { 0, 0, .55 }, 0.000001);

		
		assertArrayEquals(if2.getLower(0, 0), new double[] { .1, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(1, 0), new double[] { 0, .1, 0 }, 0.000001);
		assertArrayEquals(if2.getLower(2, 0), new double[] { 0, 0, .1 }, 0.000001);

		assertArrayEquals(if2.getUpper(0, 0), new double[] { .15, 0, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(1, 0), new double[] { 0, .15, 0 }, 0.000001);
		assertArrayEquals(if2.getUpper(2, 0), new double[] { 0, 0, .15 }, 0.000001);

	}

}
