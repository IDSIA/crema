package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.model.io.TypesIO;

import java.util.HashMap;
import java.util.Map;

public enum UAITypes implements TypesIO {

	BAYES("BAYES"),
	EVID("EVID"),
	HCREDAL("H-CREDAL"),
	VCREDAL("V-CREDAL");

	private static final Map<String, TypesIO> BY_LABEL = new HashMap<>();

	static {
		for (UAITypes t : values()) {
			BY_LABEL.put(t.label, t);
		}
	}

	public final String label;

	public String getLabel() {
		return label;
	}

	UAITypes(String label) {
		this.label = label;
	}

	public String toString() {
		return this.label;
	}

	public static TypesIO valueOfLabel(String label) {
		if (!BY_LABEL.containsKey(label))
			throw new java.lang.IllegalArgumentException("No enum constant " + label);
		return BY_LABEL.get(label);
	}

}