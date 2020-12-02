package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.IO;
import ch.idsia.crema.model.graphical.DAGModel;
import org.junit.Test;

import java.io.IOException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 12:02
 */
public class VCredalUAIWriterTest {

	@Test
	public void writeSimpleVCredalUai() throws IOException {
		String fileName = "./models/simple-vcredal_";

		DAGModel model;

		model = (DAGModel) UAIParser.read(fileName + ".uai");
		UAIWriter.write(model, fileName + "2.uai");

		model = (DAGModel) UAIParser.read(fileName + "2.uai");
		IO.write(model, fileName + "3.uai");
	}


}