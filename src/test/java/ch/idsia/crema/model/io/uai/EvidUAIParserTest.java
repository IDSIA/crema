package ch.idsia.crema.model.io.uai;


import gnu.trove.map.TIntIntMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.12.2020 11:51
 */
public class EvidUAIParserTest {

	@Test
	public void readEvidenceSimpleUaiDo() throws IOException {
		// .uai.do and .uai.evid are parsed so far in the same way
		TIntIntMap[] evidences = UAIParser.read("./models/simple.uai.do");

		for (TIntIntMap ev : evidences)
			System.out.println(ev);
	}
}