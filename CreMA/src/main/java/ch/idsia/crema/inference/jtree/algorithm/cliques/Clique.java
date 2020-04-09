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

	/**
	 * Checks if this clique contains all the elements of the other clique
	 *
	 * @param other another clique
	 * @return true if other clique is contained by this, otherwise false
	 */
	public boolean contains(Clique other) {
		if (variables.length < other.variables.length)
			return false;

		for (int v : other.variables) {
			int b = Arrays.binarySearch(variables, v);
			if (b < 0)
				return false;
		}

		return true;
	}

	/**
	 * Computes the intersection between the variables of this clique and another one.
	 *
	 * @param other another clique
	 * @return an array with the index of the elements in the intersection
	 */
	public int[] intersection(Clique other) {
		return ArraysUtil.intersectionSorted(this.variables, other.variables);
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
