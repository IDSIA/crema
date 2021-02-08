package ch.idsia.crema.model.io.bif;

import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.01.2021 09:31
 */
public class BIFParserTest {

	@Test
	public void testReadAsiaBif() throws IOException {
		String filename = "models/bif/asia.bif";

		BIFObject obj = BIFParser.read(filename);

		final BayesianNetwork bn = obj.network;
		final Map<String, Integer> map = obj.variableName;

		assertEquals(8, bn.getVariables().length);
		for (int v : bn.getVariables()) {
			assertEquals(2, bn.getSize(v));
		}

		assertEquals(0, bn.getParents(map.get("asia")).length, "asia");
		assertEquals(1, bn.getParents(map.get("tub")).length, "tub");
		assertEquals(0, bn.getParents(map.get("smoke")).length, "smoke");
		assertEquals(1, bn.getParents(map.get("lung")).length, "lung");
		assertEquals(1, bn.getParents(map.get("bronc")).length, "bronc");
		assertEquals(2, bn.getParents(map.get("either")).length, "either");
		assertEquals(1, bn.getParents(map.get("xray")).length, "xray");
		assertEquals(2, bn.getParents(map.get("dysp")).length, "dysp");

		assertArrayEquals(new double[]{.01, .99}, obj.variableFactors.get("asia").getData());
		assertArrayEquals(new double[]{.05, .95, .01, .99}, obj.variableFactors.get("tub").getData());
		assertArrayEquals(new double[]{.5, .5}, obj.variableFactors.get("smoke").getData());
		assertArrayEquals(new double[]{.1, .9, .01, .99}, obj.variableFactors.get("lung").getData());
		assertArrayEquals(new double[]{.6, .4, .3, .7}, obj.variableFactors.get("bronc").getData());
		assertArrayEquals(new double[]{1., 0., 1., 0., 1., 0., 0., 1.}, obj.variableFactors.get("either").getData());
		assertArrayEquals(new double[]{.98, .02, .05, .95}, obj.variableFactors.get("xray").getData());
		assertArrayEquals(new double[]{.9, .1, .7, .3, .8, .2, .1, .9}, obj.variableFactors.get("dysp").getData());
	}

	@Test
	public void testReadCancerBif() throws IOException {
		String filename = "models/bif/cancer.bif";

		BIFObject obj = BIFParser.read(filename);

		final BayesianNetwork bn = obj.network;
		final Map<String, Integer> map = obj.variableName;

		assertEquals(5, bn.getVariables().length);
		for (int v : bn.getVariables()) {
			assertEquals(2, bn.getSize(v));
		}

		assertEquals(0, bn.getParents(map.get("Pollution")).length);
		assertEquals(0, bn.getParents(map.get("Smoker")).length);
		assertEquals(2, bn.getParents(map.get("Cancer")).length);
		assertEquals(1, bn.getParents(map.get("Xray")).length);
		assertEquals(1, bn.getParents(map.get("Dyspnoea")).length);

		assertArrayEquals(new double[]{.9, .1}, obj.variableFactors.get("Pollution").getData());
		assertArrayEquals(new double[]{.3, .7}, obj.variableFactors.get("Smoker").getData());
		assertArrayEquals(new double[]{.03, .97, .05, .95, .001, .999, .02, .98}, obj.variableFactors.get("Cancer").getData());
		assertArrayEquals(new double[]{.9, .1, .2, .8}, obj.variableFactors.get("Xray").getData());
		assertArrayEquals(new double[]{.65, .35, .3, .7}, obj.variableFactors.get("Dyspnoea").getData());
	}

	@Test
	public void testReadEarthquakeBif() throws IOException {
		String filename = "models/bif/earthquake.bif";

		BIFObject obj = BIFParser.read(filename);

		final BayesianNetwork bn = obj.network;
		final Map<String, Integer> map = obj.variableName;

		assertEquals(5, bn.getVariables().length);
		for (int v : bn.getVariables()) {
			assertEquals(2, bn.getSize(v));
		}

		assertEquals(0, bn.getParents(map.get("Burglary")).length);
		assertEquals(0, bn.getParents(map.get("Earthquake")).length);
		assertEquals(2, bn.getParents(map.get("Alarm")).length);
		assertEquals(1, bn.getParents(map.get("JohnCalls")).length);
		assertEquals(1, bn.getParents(map.get("MaryCalls")).length);

		assertArrayEquals(new double[]{.01, .99}, obj.variableFactors.get("Burglary").getData());
		assertArrayEquals(new double[]{.02, .98}, obj.variableFactors.get("Earthquake").getData());
		assertArrayEquals(new double[]{.95, .05, .29, .71, .94, .06, .001, .999}, obj.variableFactors.get("Alarm").getData());
		assertArrayEquals(new double[]{.9, .1, .05, .95}, obj.variableFactors.get("JohnCalls").getData());
		assertArrayEquals(new double[]{.7, .3, .01, .99}, obj.variableFactors.get("MaryCalls").getData());
	}

	@Test
	public void testReadSachsBif() throws IOException {
		String filename = "models/bif/sachs.bif";

		BIFObject obj = BIFParser.read(filename);

		final BayesianNetwork bn = obj.network;
		final Map<String, Integer> map = obj.variableName;

		assertEquals(11, bn.getVariables().length);
		for (int v : bn.getVariables()) {
			assertEquals(3, bn.getSize(v));
		}

		assertEquals(2, bn.getParents(map.get("Akt")).length);
		assertEquals(2, bn.getParents(map.get("Erk")).length);
		assertEquals(2, bn.getParents(map.get("Jnk")).length);
		assertEquals(3, bn.getParents(map.get("Mek")).length);
		assertEquals(2, bn.getParents(map.get("P38")).length);
		assertEquals(2, bn.getParents(map.get("PIP2")).length);
		assertEquals(1, bn.getParents(map.get("PIP3")).length);
		assertEquals(1, bn.getParents(map.get("PKA")).length);
		assertEquals(0, bn.getParents(map.get("PKC")).length);
		assertEquals(0, bn.getParents(map.get("Plcg")).length);
		assertEquals(2, bn.getParents(map.get("Raf")).length);
	}

}