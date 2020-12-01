package ch.idsia.crema.model.io;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.io.uai.UAIParser;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("rawtypes")
public class UAIParserTest {

	Map<String, Object> models;

	@BeforeAll
	public void init() throws IOException {
		String modelFolder = "./models/";
		String[] names = {"simple-hcredal.uai", "simple-vcredal.uai", "simple-bayes.uai"};

		models = new HashMap<>();
		for (String name : names) {
			models.put(name, UAIParser.read(modelFolder + name));
		}
	}

	@ParameterizedTest
	@CsvSource(value = {"simple-hcredal.uai:3", "simple-vcredal.uai:3"}, delimiter = ':')
	void numvars(String name, String num) {
		System.out.println(models.get(name));

		assertEquals(((DAGModel) models.get(name)).getVariables().length, Integer.parseInt(num));
	}

	@ParameterizedTest
	@ValueSource(strings = {"simple-hcredal.uai", "simple-vcredal.uai"})
	void checkDomains(String name) {
		assertTrue(((DAGModel) models.get(name)).correctFactorDomains());
	}

	@ParameterizedTest
	@ValueSource(strings = {"simple-hcredal.uai"})
	void checkLinearProg(String name) {
		DAGModel model = ((DAGModel) models.get(name));
		((SeparateHalfspaceFactor) model.getFactor(0)).getRandomVertex(0);

	}

	@Test
	void checkBayesNormalized() {
		BayesianNetwork bnet = (BayesianNetwork) models.get("simple-bayes.uai");
		Assert.assertArrayEquals(bnet.getFactor(1).marginalize(1).getData(), new double[]{1., 1., 1.}, 0.0);
	}

	@Test
	void testVmodel() throws IOException {
		DAGModel model = (DAGModel) UAIParser.read("./models/simple-vcredal2.uai");
		VertexFactor vfactor = (VertexFactor) model.getFactor(2);
		assertEquals(0.3, vfactor.getData()[1][0][0], 0.000001);
	}
}
