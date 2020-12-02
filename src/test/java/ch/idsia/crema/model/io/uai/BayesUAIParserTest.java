package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:36
 */
public class BayesUAIParserTest {

	@Test
	public void readSimpleBayesUAi() throws IOException {
		String fileName = "./models/simple-bayes.uai"; // .cn File to open

		BayesianNetwork model = (BayesianNetwork) UAIParser.read(fileName);

		Assert.assertEquals("Wrong number of variables", 2, model.getVariablesCount());
		Assert.assertEquals("P[0] is not parent of P[1]", 1, model.getParents(1).length);

		BayesianFactor f0 = model.getFactor(0);
		BayesianFactor f1 = model.getFactor(1);

		Assert.assertArrayEquals("Factor 0 has wrong CPT", new double[]{.7, .2, .1}, f0.getData(), 1e-3);
		Assert.assertArrayEquals("Factor 1 has wrong CPT", new double[]{.2, .7, .9, .8, .3, .1}, f1.getData(), 1e-3);
	}
}