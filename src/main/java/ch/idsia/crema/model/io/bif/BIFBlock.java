package ch.idsia.crema.model.io.bif;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.01.2021 10:41
 */
class BIFBlock {
	String type;
	String name;
	List<BIFAttribute> attributes = new ArrayList<>();

	@Override
	public String toString() {
		return type + " " + String.join(" ", name) + " {\n" +
				attributes.stream().map(Object::toString).collect(Collectors.joining("\n  ")) +
				"\n}";
	}
}
