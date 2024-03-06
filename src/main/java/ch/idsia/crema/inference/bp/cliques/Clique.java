package ch.idsia.crema.inference.bp.cliques;

import ch.idsia.crema.utility.ArraysUtil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    13.02.2018 14:34
 */
public class Clique {

	/**
	 * Variables covered by the {@link Clique}.
	 */
	private int[] variables;
	/**
	 * Variable that created the {@link Clique}. This should be the potential assigned to this clique.
	 */
	private final IntList v = new IntArrayList();

	protected Clique() {

	}

	/**
	 * Creates a new Clique from a sorted array of variables.
	 *
	 * @param variables must be sorted
	 */
	public Clique(int[] variables) {
		this.variables = variables;
	}

	/**
	 * Creates a new Clique from a sorted array of variables and the array that created the clique.
	 *
	 * @param v         this is the variable that was removed from the {@link FindCliques} algorithm to create
	 *                  this {@link Clique}.
	 * @param variables must be sorted
	 */
	public Clique(int v, int[] variables) {
		this.variables = variables;
		this.v.add(v);
	}

	public int[] getVariables() {
		return variables;
	}

	public IntList getV() {
		return v;
	}

	public int[] getVArray() {
		return v.toIntArray();
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
	 * Checks if this {@link Clique} contains, between its variables, the given variable.
	 *
	 * @param variable variable to search for
	 * @return true if the variable is found, otherwise false
	 */
	public boolean contains(int variable) {
		int i = Arrays.binarySearch(variables, variable);
		return i >= 0;
	}

	/**
	 * Checks if this {@link Clique} contains all the given variables.
	 *
	 * @param variables variables to check for
	 * @return true if all the variables are found, otherwise false
	 */
	public boolean containsAll(int[] variables) {
		for (int v : variables) {
			if (!contains(v))
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
		return "Clique" + Arrays.toString(variables);
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
