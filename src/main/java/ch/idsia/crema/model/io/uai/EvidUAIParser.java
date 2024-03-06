package ch.idsia.crema.model.io.uai;


import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;
import java.util.List;

/**
 * Parser for evidence files in UAI format
 *
 * @author Rafael Cabañas
 */
public class EvidUAIParser extends UAIParser<Int2IntMap[]> {

	private int[][][] evid;

	public EvidUAIParser(String filename) throws IOException {
		super(filename);
	}

	public EvidUAIParser(List<String> lines) {
		super(lines);
		TYPE = UAITypes.EVID;
	}

	@Override
	protected void processFile() {
		parseType();

		int numEvidences = popInteger();
		evid = new int[numEvidences][][];
		for (int i = 0; i < numEvidences; i++) {
			int numObserved = popInteger();
			evid[i] = new int[numObserved][2];
			for (int j = 0; j < numObserved; j++) {
				evid[i][j][0] = popInteger();
				evid[i][j][1] = popInteger();
			}
		}
	}

	@Override
	protected Int2IntMap[] build() {
		Int2IntMap[] parsedObject = new Int2IntMap[evid.length];
		for (int i = 0; i < evid.length; i++) {
			parsedObject[i] = new Int2IntOpenHashMap();
			for (int j = 0; j < evid[i].length; j++) {
				parsedObject[i].put(evid[i][j][0], evid[i][j][1]);
			}
		}

		return parsedObject;
	}

}
