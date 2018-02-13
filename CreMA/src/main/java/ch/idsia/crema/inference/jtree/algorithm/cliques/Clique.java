package ch.idsia.crema.inference.jtree.algorithm.cliques;

import ch.idsia.crema.utility.ArraysUtil;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    13.02.2018 14:34
 */
public class Clique {

	private int[] variables;

	/**
	 * Creates a new Clique from a sorted array of variables.
	 *
	 * @param variables must be sorted
	 */
	public Clique(int[] variables) {
		this.variables = variables;
	}

	public int[] getVariables() {
		return variables;
	}

	public boolean contains(Clique clique) {
		if (variables.length < clique.variables.length)
			return false;

		for (int v : clique.variables) {
			int b = Arrays.binarySearch(variables, v);
			if (b < 0)
				return false;
		}

		return true;
	}

	public int[] intersection(Clique clique) {
		return ArraysUtil.intersection(this.variables, clique.variables);
	}

	@Override
	public String toString() {
		return "{" + Arrays.toString(variables) + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Clique clique = (Clique) o;
		return Arrays.equals(variables, clique.variables);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(variables);
	}
}
