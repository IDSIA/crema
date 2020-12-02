package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.IO;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.Test;

import java.io.IOException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:46
 */
public class BayesUAIWriterTest {

	@Test
	public void readBayesUaiAndWrite() throws IOException {
		String fileName = "./models/bayes";

		BayesianNetwork bnet = (BayesianNetwork) IO.read(fileName+".uai");

		IO.write(bnet,fileName+"_2.uai");
	}
}