package ch.idsia.crema.inference.approxlp0;

import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.io.uai.UAIParser;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.08.2021 09:12
 */
class ApproxLP0Test {

	// See https://github.com/IDSIA/crepo for details on used networks

	@Test
	void marginalization() throws Exception {
		RandomUtil.setRandom(new Random(42));

		final GraphicalModel<SeparateHalfspaceFactor> model = UAIParser.read("./models/hmodel-mult_n5_mID2_mD6_mV4_nV2-1.uai");
		final int query = 1;

		final ApproxLP0<SeparateHalfspaceFactor> alp0 = new ApproxLP0<>();
		final IntervalFactor q0 = alp0.query(model, query);

		System.out.println(q0);

		final double[] qLower = q0.getLower();
		final double[] qUpper = q0.getUpper();

		final double[] eLower = new double[]{
				0.0771239927907724,
				0.049833625399563215,
				0.03209765961089037,
				0.1979838131957844,
		};

		final double[] eUpper = new double[]{
				0.4115415549163965,
				0.36225597200986337,
				0.3527710603016937,
				0.6555962542500847,
		};

		// TODO: this checks only that works

		// Assertions.assertArrayEquals(eLower, qLower, 1e-03);
		// Assertions.assertArrayEquals(eUpper, qUpper, 1e-03);
	}

	@Test
	void conditional() throws Exception {
		RandomUtil.setRandom(new Random(42));

		final GraphicalModel<SeparateHalfspaceFactor> model = UAIParser.read("./models/hmodel-mult_n5_mID2_mD6_mV4_nV2-1.uai");
		final int query = 0;
		final TIntIntMap obs = new TIntIntHashMap();
		obs.put(2, 0);
		obs.put(1, 0);

		final ApproxLP0<SeparateHalfspaceFactor> alp0 = new ApproxLP0<>();
		final IntervalFactor q0 = alp0.query(model, obs, query);

		System.out.println(q0);

		final double[] qLower = q0.getLower();
		final double[] qUpper = q0.getUpper();

		final double[] eLower = new double[]{
				0.12498535114024707,
				0.07126087005687232,
		};
		final double[] eUpper = new double[]{
				0.9287391299431277,
				0.8750146488597529,
		};

		// TODO: this checks only that works

		// Assertions.assertArrayEquals(eLower, qLower, 1e-03);
		// Assertions.assertArrayEquals(eUpper, qUpper, 1e-03);
	}

}