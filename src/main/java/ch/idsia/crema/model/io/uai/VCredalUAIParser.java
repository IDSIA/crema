package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Parser for V-CREDAL networks in UAI format
 *
 * @author Rafael Cabañas
 */

public class VCredalUAIParser extends NetUAIParser<GraphicalModel<VertexFactor>> {

	private double[][][][] vertices = new double[numberOfVariables][][][];

	public VCredalUAIParser(String filename) throws IOException {
		super(filename);
	}

	public VCredalUAIParser(List<String> lines) {
		super(lines);
		TYPE = UAITypes.VCREDAL;
	}

	@Override
	protected void processFile() {
		parseType();
		parseVariablesInfo();
		parseDomainsLastIsHead();
		parseVertices();
	}

	@Override
	protected GraphicalModel<VertexFactor> build() {
		GraphicalModel<VertexFactor> model = new DAGModel<>();

		// Add the variables
		for (int i = 0; i < numberOfVariables; i++) {
			model.addVariable(cardinalities[i]);
		}

		// Adding the parents to each variable
		for (int k = 0; k < numberOfVariables; k++) {
			model.addParents(k, parents[k]);
		}

		// Specifying the vertices
		VertexFactor[] cpt = new VertexFactor[numberOfVariables];

		for (int i = 0; i < numberOfVariables; i++) {

			cpt[i] = new VertexFactor(model.getDomain(i), model.getDomain(parents[i]), vertices[i]);
		}

		model.setFactors(cpt);

		return model;
	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();
	}

	private void parseVertices() {

		// Parsing the a and b coefficient for each variable
		vertices = new double[numberOfVariables][][][];
		for (int i = 0; i < numberOfVariables; i++) {
			int parentComb = IntStream.of(parents[i]).map(p -> cardinalities[p]).reduce((a, b) -> a * b).orElse(1);
			vertices[i] = new double[parentComb][][];

			int[] parent_list = ArraysUtil.reverse(parents[i]);
			int[] sizes = ArraysUtil.at(cardinalities, parent_list);

			Strides dataDomain = new Strides(parent_list, sizes);
			IndexIterator iter = dataDomain.getReorderedIterator(parents[i]);
			int j;

			while (iter.hasNext()) {
				j = iter.next();
				int numVertices = popInteger() / cardinalities[i];
				vertices[i][j] = new double[numVertices][];
				for (int k = 0; k < numVertices; k++) {
					vertices[i][j][k] = new double[cardinalities[i]];
					for (int s = 0; s < cardinalities[i]; s++) {
						vertices[i][j][k][s] = popDouble();
					}
				}
			}

		}
	}

}
