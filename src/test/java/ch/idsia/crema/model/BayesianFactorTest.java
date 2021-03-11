package ch.idsia.crema.model;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BayesianFactorTest {

	@Test
	public void testMarginalize() {
		int[] vars = new int[]{1, 2};
		int[] size = new int[]{3, 3};

		double[] vals = new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.10, 0.10};

		BayesianFactor ibf = new BayesianFactor(new Strides(vars, size), true);
		ibf.setData(vals);

		assertArrayEquals(new double[]{0.45, 0.2, 0.35}, ibf.marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.4, 0.2, 0.4}, ibf.marginalize(2).getData(), 1e-7);

		assertArrayEquals(new double[]{1}, ibf.marginalize(2).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{1}, ibf.marginalize(1).marginalize(2).getData(), 1e-7);

		assertArrayEquals(new int[]{}, ibf.marginalize(1).marginalize(2).getDomain().getVariables());

		assertArrayEquals(new int[]{1}, ibf.marginalize(2).getDomain().getVariables());
		assertArrayEquals(new int[]{2}, ibf.marginalize(1).getDomain().getVariables());

		vars = new int[]{1, 2, 3};
		size = new int[]{2, 2, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.10, 0.05, 0.05, 0.15, 0.20};

		ibf = new BayesianFactor(new Strides(vars, size), true);
		ibf.setData(vals);

		assertArrayEquals(new double[]{0.2, 0.35, 0.1, 0.35}, ibf.marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.4, 0.15, 0.2, 0.25}, ibf.marginalize(2).getData(), 1e-7);
		assertArrayEquals(new double[]{0.2, 0.10, 0.4, 0.30}, ibf.marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.55, 0.45}, ibf.marginalize(1).marginalize(2).getData(), 1e-7);
		assertArrayEquals(new double[]{0.3, 0.7}, ibf.marginalize(1).marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.55, 0.45}, ibf.marginalize(2).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.6, 0.4}, ibf.marginalize(2).marginalize(3).getData(), 1e-7);

		assertArrayEquals(new double[]{0.3, 0.7}, ibf.marginalize(3).marginalize(1).getData(), 1e-7);
		assertArrayEquals(new double[]{0.6, 0.4}, ibf.marginalize(3).marginalize(2).getData(), 1e-7);

	}

	@Test
	public void testCombineCommutativeProperty() {
		int[] vars = new int[]{1, 2, 3};
		int[] size = new int[]{2, 3, 2};
		double[] vals = new double[]{0.15, 0.85, 0.25, 0.75, 0.95, 0.05, 0.5, 0.5, 0.4, 0.6};

		BayesianFactor ibf = new BayesianFactor(new Strides(vars, size), true);
		ibf.setData(vals);

		vars = new int[]{2, 3};
		size = new int[]{3, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.20, 0.25, 0.1};

		BayesianFactor ibf2 = new BayesianFactor(new Strides(vars, size), true);
		ibf2.setData(vals);

		// P PA = new P(A | B, C);
		// P PA = PA_B * PB - B;

		BayesianFactor r = ibf2.combine(ibf);

		double[] data = r.getData();

		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		r = ibf.combine(ibf2);
		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		assertArrayEquals(data, r.getData(), 1e-9);
	}


	@Test
	public void testCombineDivision() {
		int[] vars = new int[]{1, 2, 3};
		int[] size = new int[]{2, 3, 2};
		double[] vals = new double[]{0.15, 0.85, 0.25, 0.75, 0.95, 0.05, 0.5, 0.5, 0.4, 0.6};

		BayesianFactor ibf = new BayesianFactor(new Strides(vars, size), true);
		ibf.setData(vals);

		vars = new int[]{2, 3};
		size = new int[]{3, 2};
		vals = new double[]{0.15, 0.05, 0.25, 0.20, 0.25, 0.1};

		BayesianFactor ibf2 = new BayesianFactor(new Strides(vars, size), true);
		ibf2.setData(vals);

		BayesianFactor r = ibf2.combine(ibf);

		double[] data = r.getData();

		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		r = ibf2.combine(ibf);
		assertArrayEquals(new int[]{1, 2, 3}, r.getDomain().getVariables());
		assertArrayEquals(new int[]{2, 3, 2}, r.getDomain().getSizes());

		assertArrayEquals(data, r.getData(), 1e-9);

		// a * b / b = a
		r = r.divide(ibf2);
		assertEquals(ibf, r);
	}

	@Test
	public void testDivideFactor() {

	}

}
