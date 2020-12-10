package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.IO;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:46
 */
public class BayesUAIWriterTest {

	@Test
	public void readBayesUaiAndWrite() throws IOException {
		String filename = "./models/bayes";

		BayesianNetwork bn = new BayesUAIParser(filename + ".uai").parse();

		IO.write(bn, filename + "_2.uai");
	}

	@Test
	public void readBayesUaiAndSerialize() throws IOException {
		String filename = "./models/bayes.uai";

		BayesianNetwork bn = new BayesUAIParser(filename).parse();

		List<String> lines = new BayesUAIWriter(bn, filename).serialize();
		BayesianNetwork bn2 = new BayesUAIParser(lines).parse();

		Assertions.assertEquals(bn.getVariables().length, bn2.getVariables().length);
		Assertions.assertEquals(bn.getNetwork().vertexSet().size(), bn2.getNetwork().vertexSet().size());
		Assertions.assertEquals(bn.getNetwork().edgeSet().size(), bn2.getNetwork().edgeSet().size());
	}
}