package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

		assertEquals(3, model.getVariablesCount(), "Wrong number of variables");
		assertEquals(1, model.getParents(1).length, "P[1] does not have exactly one parent");
		assertEquals(2, model.getParents(2).length, "P[2] does not have exactly two parents");

		// checks for parents
		assertTrue(Arrays.stream(model.getParents(1)).anyMatch(x -> x == 0));
		assertTrue(Arrays.stream(model.getParents(2)).anyMatch(x -> x == 0));
		assertTrue(Arrays.stream(model.getParents(2)).anyMatch(x -> x == 1));

		// TODO: check content of factors

		for (int x : model.getVariables()) {
			System.out.println(x);
			System.out.println(model.getFactor(x));
		}
	}

}