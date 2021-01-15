package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.ArraysUtil;

import java.io.IOException;
import java.util.List;

/**
 * Parser for Bayesian Networks in UAI format
 *
 * @author Rafael Cabañas
 */
public class BayesUAIParser extends NetUAIParser<BayesianNetwork> {

	private double[][] probs;

	public BayesUAIParser(String filename) throws IOException {
		super(filename);
	}

	public BayesUAIParser(List<String> lines) {
		super(lines);
		TYPE = UAITypes.BAYES;
	}

	@Override
	protected void processFile() {
		parseType();
		parseVariablesInfo();
		parseDomainsLastIsHead();
		parseCPTs();
	}

	@Override
	protected BayesianNetwork build() {
		BayesianNetwork model = new BayesianNetwork();

		// Add the variables
		for (int i = 0; i < numberOfVariables; i++) {
			model.addVariable(cardinalities[i]);
		}

		// Adding the parents to each variable
		for (int k = 0; k < numberOfVariables; k++) {
			model.addParents(k, parents[k]);
		}

		// Build the bayesian Factor for each variable
		BayesianFactor[] cpt = new BayesianFactor[numberOfVariables];
		for (int i = 0; i < numberOfVariables; i++) {
			// Build the domain with the head/left variable at the end
			Strides dom = model.getDomain(parents[i]).concat(model.getDomain(i));

			double[] data = probs[i];
			if (parents[i].length > 0)
				data = ArraysUtil.changeEndian(probs[i], dom.getSizes());
			cpt[i] = new BayesianFactor(dom, data);

		}

		model.setFactors(cpt);

		return model;
	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();
	}

	/**
	 * Parse the probability values and store them in a 1D array for each factor.
	 */
	private void parseCPTs() {
		probs = new double[numberOfVariables][];

		for (int i = 0; i < numberOfVariables; i++) {
			int numValues = popInteger();
			probs[i] = new double[numValues];
			for (int j = 0; j < numValues; j++) {
				probs[i][j] = popDouble();
			}
		}
	}

}
