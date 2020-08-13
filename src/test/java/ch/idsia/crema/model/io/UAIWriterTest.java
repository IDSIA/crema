package ch.idsia.crema.model.io;

import ch.idsia.crema.IO;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.io.uai.UAIParser;
import ch.idsia.crema.model.io.uai.UAIWriter;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class UAIWriterTest {

	@Test
	void testHmodel() throws IOException {
		String fileName = "./models/test/simple-hcredal";

		SparseModel model;

		model = (SparseModel) UAIParser.read(fileName+".uai");
		UAIWriter.write(model,fileName+"2.uai");

		FileReader fileReader = new FileReader(fileName+"2.uai");
		BufferedReader reader = new BufferedReader(fileReader);

		String text = reader.lines().collect(Collectors.joining());

		String expected = "H-CREDAL 3 2 2 3 3 1 0 2 0 1 3 0 1 2 8 1.0 0.0" +
				" -1.0 0.0 0.0 1.0 0.0 -1.0 4 0.3 -0.2 0.8 -0.6 32 1.0 0.0" +
				" 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 -1.0 0.0 0.0" +
				" 0.0 0.0 1.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0" +
				" -1.0 8 0.5 -0.1 0.9 -0.4 0.2 -0.1 0.9 -0.5 288 -1.0 0.0 0.0" +
				" 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 0.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0" +
				" 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0" +
				" 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0" +
				" 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0" +
				" 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 -1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 24 -0.11 0.9" +
				" -0.12 0.9 -0.13 0.9 -0.31 0.9 -0.32 0.9 -0.33 0.9 -0.21 0.9 -0.22 0.9 -0.23 0.9 -0.41 0.9 -0.42 0.9 -0.43 0.9 ";

		assertEquals(expected, text);
	}

	@Test
	void testVmodel() throws IOException {
		String fileName = "./models/test/simple-vcredal";

		SparseModel model;

		model = (SparseModel) UAIParser.read(fileName+".uai");
		UAIWriter.write(model,fileName+"2.uai");

		FileReader fileReader = new FileReader(fileName+"2.uai");
		BufferedReader reader = new BufferedReader(fileReader);

		String text = reader.lines().collect(Collectors.joining());

		String expected = "V-CREDAL 3 2 3 3 3 1 0 2 0 1 3 0 1 2 4 0.2 0.8 0.3 0.7 6 0.1 0.9 0.0 0.55 0.35 0.1 6 0.1 0.9 " +
				"0.0 0.2 0.5 0.3 9 0.1 0.2 0.8 0.1 0.1 0.8 0.1 0.8 0.1 9 0.2 0.2 0.6 0.2 0.1 0.7 0.2 0.7 0.1 9 0.3 0.2 " +
				"0.5 0.3 0.1 0.6 0.3 0.4 0.3 9 0.4 0.3 0.3 0.4 0.1 0.5 0.4 0.6 0.0 9 0.5 0.4 0.1 0.5 0.1 0.4 0.5 0.5 " +
				"0.0 9 0.6 0.4 0.0 0.6 0.1 0.3 0.6 0.2 0.2 ";

		assertEquals(expected, text);
	}

}
