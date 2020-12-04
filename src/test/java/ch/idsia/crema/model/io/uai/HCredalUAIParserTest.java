package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:53
 */
public class HCredalUAIParserTest {

	@Test
	public void readSimpleHCredalUai() throws IOException {
		String fileName = "./models/simple-hcredal.uai";
		GraphicalModel<SeparateHalfspaceFactor> model = UAIParser.read(fileName);

		Assert.assertEquals("Wrong number of variables", 3, model.getVariablesCount());
		Assert.assertEquals("P[1] does not have exactly one parent", 1, model.getParents(1).length);
		Assert.assertEquals("P[2] does not have exactly two parents", 2, model.getParents(2).length);

		// checks for parents
		Assert.assertTrue(Arrays.stream(model.getParents(1)).anyMatch(x -> x == 0));
		Assert.assertTrue(Arrays.stream(model.getParents(2)).anyMatch(x -> x == 0));
		Assert.assertTrue(Arrays.stream(model.getParents(2)).anyMatch(x -> x == 1));

		// TODO: check content of factors

		for (int i = 0; i < model.getVariables().length; i++) {
			System.out.println("Variable " + i);
			model.getFactor(i).printLinearProblem();
		}
	}

}