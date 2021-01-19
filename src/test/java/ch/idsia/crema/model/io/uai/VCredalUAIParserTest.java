package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:59
 */
public class VCredalUAIParserTest {

	@Test
	public void readSimpleVCredal2Uai() throws IOException {
		String fileName = "./models/simple-vcredal2.uai";
		GraphicalModel<VertexFactor> model = UAIParser.read(fileName);

		Assert.assertEquals("Wrong number of variables", 3, model.getVariablesCount());
		Assert.assertEquals("P[1] does not have exactly one parent", 1, model.getParents(1).length);
		Assert.assertEquals("P[2] does not have exactly two parents", 2, model.getParents(2).length);

		// checks for parents
		Assert.assertTrue(Arrays.stream(model.getParents(1)).anyMatch(x -> x == 0));
		Assert.assertTrue(Arrays.stream(model.getParents(2)).anyMatch(x -> x == 0));
		Assert.assertTrue(Arrays.stream(model.getParents(2)).anyMatch(x -> x == 1));

		// TODO: check content of factors

		for (int x : model.getVariables()) {
			System.out.println(x);
			System.out.println(model.getFactor(x));
		}
	}

}