package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.optim.linear.Relationship;

import java.io.IOException;
import java.util.List;

/**
 * Parser for H-CREDAL networks in UAI format
 *
 * @author Rafael Cabañas
 */
public class HCredalUAIParser extends NetUAIParser<GraphicalModel<SeparateHalfspaceFactor>> {

	private double[][] aCoeff = new double[numberOfVariables][];
	private double[][] bCoeff = new double[numberOfVariables][];

	public HCredalUAIParser(String filename) throws IOException {
		super(filename);
	}

	public HCredalUAIParser(List<String> lines) {
		super(lines);
		TYPE = UAITypes.HCREDAL;
	}

	@Override
	protected void processFile() {
		parseType();
		parseVariablesInfo();
		parseDomainsLastIsHead();
		parseCoefficients();
	}

	@Override
	protected GraphicalModel<SeparateHalfspaceFactor> build() {
		GraphicalModel<SeparateHalfspaceFactor> model = new DAGModel<>();

		// Add the variables
		for (int i = 0; i < numberOfVariables; i++) {
			model.addVariable(cardinalities[i]);
		}

		// Adding the parents to each variable
		for (int k = 0; k < numberOfVariables; k++) {
			model.addParents(k, parents[k]);
		}

		// Specifying the linear constraints for each variable
		SeparateHalfspaceFactor[] cpt = new SeparateHalfspaceFactor[numberOfVariables];
		for (int i = 0; i < numberOfVariables; i++) {
			int varsize = model.getDomain(i).getSizes()[0];

			if (parents[i].length == 0) {
				// reshaped coeff A
				double[][] A2d = ArraysUtil.reshape2d(aCoeff[i], aCoeff[i].length / varsize);
				// build the factor
				cpt[i] = SeparateHalfspaceFactorFactory.factory().left(model.getDomain(i)).constraints(A2d, bCoeff[i], Relationship.LEQ).get();
			} else {

				int par_comb = model.getDomain(model.getParents(i)).getCombinations();
				double[][] A2d_full = ArraysUtil.reshape2d(aCoeff[i], aCoeff[i].length / (varsize * par_comb));

				// Get the coefficients matrices for each combination of the parents
				double[][][] A = new double[par_comb][][];
				double[][] b = new double[par_comb][];

				// get a reordered iterator as UAI stores data with inverted variables compared to Crema
				Strides dataDomain = model.getDomain(model.getParents(i)).reverseDomain();
				IndexIterator iter = dataDomain.getReorderedIterator(model.getParents(i));

				for (int j = 0; j < par_comb; j++) {
					int jj = iter.next();
					double[][] Aj = ArraysUtil.copyOfRange(A2d_full, j * varsize, (j + 1) * varsize, 1);
					int[] to_drop = ArraysUtil.rowsWhereAllZeros(Aj);
					Aj = ArraysUtil.dropRows(Aj, to_drop);
					double[] bj = ArraysUtil.dropColumns(new double[][]{bCoeff[i]}, to_drop)[0];
					A[jj] = Aj; // was A[j]
					b[jj] = bj;
				}

				// build the factor
				cpt[i] = SeparateHalfspaceFactorFactory.factory().domain(model.getDomain(i), model.getDomain(model.getParents(i)))
						.constraints(A, b, Relationship.LEQ)
						.get();

			}
		}

		model.setFactors(cpt);

		return model;
	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();
	}

	private void parseCoefficients() {

		// Parsing the a and b coefficient for each variable
		aCoeff = new double[numberOfVariables][];
		bCoeff = new double[numberOfVariables][];
		int n_a, n_b;
		for (int i = 0; i < numberOfVariables; i++) {
			n_a = popInteger();
			aCoeff[i] = new double[n_a];
			for (int k = 0; k < n_a; k++) {
				aCoeff[i][k] = popDouble();
			}

			n_b = popInteger();
			bCoeff[i] = new double[n_b];
			for (int k = 0; k < n_b; k++) {
				bCoeff[i][k] = popDouble();
			}
		}
	}

}
