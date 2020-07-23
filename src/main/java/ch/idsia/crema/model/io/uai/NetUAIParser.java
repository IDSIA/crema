package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.GenericSparseModel;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;

import java.util.stream.IntStream;

/**
 * Generic Parser class for PGMs in UAI format
 * 
 * @author Rafael Cabañas
 */

public abstract class NetUAIParser<T extends GenericSparseModel> extends UAIParser<T> {

	protected int numberOfVariables;
	protected int[] cardinalities;
	int numberOfTables;
	int[][] parents;

	// todo: this assume that variables take consecutive ids from 0
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
		int numberOfParents = 0;
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
		int numberOfParents = 0;
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
		Assert.isTrue(numberOfVariables == numberOfTables,
				"Wrong number of tables (" + numberOfTables + ") and variables (" + numberOfVariables + ")");

		Assert.isTrue(IntStream.of(cardinalities).allMatch(c -> c > 1), "Wrong cardinalities");

		for (int i = 0; i < parents.length; i++) {
			for (int j = 0; j < parents[i].length; j++) {
				Assert.isTrue(parents[i][j] >= 0 || parents[i][j] < numberOfVariables);
				Assert.isTrue(parents[i][j] != i);

			}
		}

	}

	protected Strides reverseDomain(Strides domain) {
		int[] vars = domain.getVariables().clone();
		int[] sizes = domain.getSizes().clone();
		ArrayUtils.reverse(vars);
		ArrayUtils.reverse(sizes);
		return new Strides(vars, sizes);
	}

}
