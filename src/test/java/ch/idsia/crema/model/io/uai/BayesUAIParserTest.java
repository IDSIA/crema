package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:36
 */
public class BayesUAIParserTest {

	@Test
	public void readSimpleBayesUAi() throws IOException {
		String fileName = "./models/simple-bayes.uai"; // .cn File to open

		BayesianNetwork model = UAIParser.read(fileName);

		assertEquals(2, model.getVariablesCount(), "Wrong number of variables");
		assertEquals(1, model.getParents(1).length, "P[0] is not parent of P[1]");

		BayesianFactor f0 = model.getFactor(0);
		BayesianFactor f1 = model.getFactor(1);

		assertArrayEquals(new double[]{.7, .2, .1}, f0.getData(), 1e-3, "Factor 0 has wrong CPT");
		assertArrayEquals(new double[]{.2, .7, .9, .8, .3, .1}, f1.getData(), 1e-3, "Factor 1 has wrong CPT");
	}
}