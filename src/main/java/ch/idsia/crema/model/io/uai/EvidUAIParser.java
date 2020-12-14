package ch.idsia.crema.model.io.uai;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.IOException;
import java.util.List;

/**
 * Parser for evidence files in UAI format
 *
 * @author Rafael Cabañas
 */
public class EvidUAIParser extends UAIParser<TIntIntMap[]> {

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
	protected TIntIntMap[] build() {
		TIntIntMap[] parsedObject = new TIntIntMap[evid.length];
		for (int i = 0; i < evid.length; i++) {
			parsedObject[i] = new TIntIntHashMap();
			for (int j = 0; j < evid[i].length; j++) {
				parsedObject[i].put(evid[i][j][0], evid[i][j][1]);
			}
		}

		return parsedObject;
	}

}
