package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexDefaultFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.08.2021 15:02
 */
class VertexToRandomBayesianTest {

	@Test
	void simpleVertexToBayesian() {
		RandomUtil.setRandomSeed(42);

		final VertexFactor v = new VertexDefaultFactor(
				Strides.var(0, 3),
				Strides.var(1, 2),
				List.of(
						new double[]{.1, .5, .4},
						new double[]{.2, .5, .3},
						new double[]{.3, .5, .2},
						new double[]{.4, .5, .1}
				),
				new TIntArrayList(new int[]{0, 0, 1, 1})
		);

		final BayesianFactor b = new VertexToRandomBayesian().apply(v, 0);
		final BayesianFactor f1 = b.filter(1, 0);
		final BayesianFactor f2 = b.filter(1, 1);
		final BayesianFactor f3 = b.filter(0, 1);

		System.out.println(v);
		System.out.println(b);
		System.out.println(f1);
		System.out.println(f2);
		System.out.println(f3);

		// TODO: this checks only that everything works, check also that it is correct!!!
	}

}