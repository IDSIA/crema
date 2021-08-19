package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.08.2021 13:28
 */
class BayesianToVertexTest {

	// TODO: add others

	@Test
	void priorConversion() {

		final BayesianDefaultFactor f = new BayesianDefaultFactor(
				new Strides(new int[]{0}, new int[]{2}),
				new double[]{.4, .6}
		);

		final VertexFactor v = new BayesianToVertex().apply(f, 0);

		// with just one vector we should get the exact input factor
		final BayesianFactor f1 = new VertexToRandomBayesian().apply(v, 0);

		Assertions.assertArrayEquals(f.getData(), f1.getData(), 1e-3);
	}

	@Test
	void conditionalConversion() {

		final BayesianDefaultFactor f = new BayesianDefaultFactor(
				new Strides(new int[]{0, 1}, new int[]{2, 2}),
				new double[]{.4, .3, .6, .7}
		);

		final VertexFactor v = new BayesianToVertex().apply(f, 0);

		// with just one vector we should get the exact input factor
		final BayesianFactor f1 = new VertexToRandomBayesian().apply(v, 0);

		Assertions.assertArrayEquals(f.getData(), f1.getData(), 1e-3);
	}
}