package ch.idsia.crema.model.io.bif;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.01.2021 10:41
 */
class BIFAttribute {
	String name;
	String value;

	@Override
	public String toString() {
		return "  " + name + " " + value;
	}
}
