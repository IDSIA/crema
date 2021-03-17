package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Generic Parser class for PGMs in UAI format
 *
 * @author Rafael Cabañas
 */
public abstract class NetUAIParser<T extends GraphicalModel<? extends OperableFactor<?>>> extends UAIParser<T> {

	protected int numberOfVariables;
	protected int[] cardinalities;
	protected int numberOfTables;
	protected int[][] parents;

	public NetUAIParser(String filename) throws IOException {
		super(filename);
	}

	public NetUAIParser(List<String> lines) {
		super(lines);
	}

	// TODO: this assume that variables take consecutive ids from 0
	protected void parseVariablesInfo() {
		// Parsing the number of variables in the network
		numberOfVariables = popInteger();
		// Parse the number of states of each variable
		cardinalities = new int[numberOfVariables];
		for (int i = 0; i < numberOfVariables; i++) {
			cardinalities[i] = popInteger();
			assert cardinalities[i] > 1;
		}
	}

	protected void parseDomainsFirstIsHead() {
		numberOfTables = popInteger();

		// Parsing the number of parents and the parents
		parents = new int[numberOfTables][];
		int numberOfParents;
		for (int i = 0; i < numberOfTables; i++) {
			numberOfParents = popInteger() - 1;
			int left_var = popInteger();
			if (parents[left_var] != null)
				throw new IllegalArgumentException(
						"Error: domain of factor associated to " + left_var + " is defined twice");
			parents[left_var] = new int[numberOfParents];
			for (int k = 0; k < numberOfParents; k++) {
				parents[i][k] = popInteger();
			}
		}
	}

	protected void parseDomainsLastIsHead() {
		numberOfTables = popInteger();

		// Parsing the number of parents and the parents
		parents = new int[numberOfTables][];
		int numberOfParents;
		for (int i = 0; i < numberOfTables; i++) {
			numberOfParents = popInteger() - 1;

			int[] parents_aux = new int[numberOfParents];
			for (int k = 0; k < numberOfParents; k++) {
				parents_aux[k] = popInteger();
			}

			int left_var = popInteger();
			if (parents[left_var] != null)
				throw new IllegalArgumentException(
						"Error: domain of factor associated to " + left_var + " is defined twice");
			parents[left_var] = parents_aux;
		}
	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();

		// Specific sanity checks for SparseModels
		if (numberOfVariables != numberOfTables)
			throw new IllegalArgumentException("Wrong number of tables (" + numberOfTables + ") and variables (" + numberOfVariables + ")");

		if (!IntStream.of(cardinalities).allMatch(c -> c > 1))
			throw new IllegalArgumentException("Wrong cardinalities");

		for (int i = 0; i < parents.length; i++) {
			for (int j = 0; j < parents[i].length; j++) {

				if (!(parents[i][j] >= 0 || parents[i][j] < numberOfVariables))
					throw new IllegalArgumentException("Wrong parents for i=" + i + " and j=" + j + ": invalid parent");

				if (parents[i][j] == i)
					throw new IllegalArgumentException("Wrong parents for i=" + i + " and j=" + j + ": parent of itself");
			}
		}
	}

	public int getNumberOfVariables() {
		return numberOfVariables;
	}

	public int[] getCardinalities() {
		return cardinalities;
	}

	public int getNumberOfTables() {
		return numberOfTables;
	}

	public int[][] getParents() {
		return parents;
	}

}
