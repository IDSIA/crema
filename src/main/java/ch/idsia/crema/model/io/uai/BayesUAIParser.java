package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
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

	protected BayesianNetwork model;
	protected BayesianFactor[] factors;


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
		buildModel();
		parseFactors();
	}

	private void buildModel() {
		model = new BayesianNetwork();

		// Add the variables
		for (int i = 0; i < numberOfVariables; i++) {
			model.addVariable(cardinalities[i]);
		}

		// Adding the parents to each variable
		for (int k = 0; k < numberOfVariables; k++) {
			model.addParents(k, parents[k]);
		}
	}


	@Override
	protected BayesianNetwork build() {
		model.setFactors(factors);

		return model;
	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();
	}

	/**
	 * Parse the probability values and store them in a 1D array for each factor.
	 */
	private void parseFactors() {
		// Build the bayesian Factor for each variable
		factors = new BayesianFactor[numberOfVariables];

		for (int i = 0; i < numberOfVariables; i++) {
			// Build the domain with the head/left variable at the end
			final Domain dom = model.getDomain(parents[i]).concat(model.getDomain(i));

			try {
				double[] data = popDoubles();

				if (parents[i].length > 0)
					data = ArraysUtil.changeEndian(data, dom.getSizes());
				factors[i] = BayesianFactorFactory.factory().domain(dom).data(data).get();

			} catch (Exception ignored) {
				// logic factors
				setOffset(getOffset() - 1);
				String e = popElement();

				switch (e) {
					case "AND": {
						final int[] parents = popIntegers();
						final int[] trueStates = popIntegers();

						factors[i] = BayesianFactorFactory.factory().domain(dom).and(parents, trueStates);

						break;
					}
					case "OR": {
						final int[] parents = popIntegers();
						final int[] trueStates = popIntegers();

						factors[i] = BayesianFactorFactory.factory().domain(dom).or(parents, trueStates);

						break;
					}
					case "NOISY-OR": {
						final double[] inhs = popDoubles();
						final int[] pars = popIntegers();
						final int[] trus = popIntegers();

						factors[i] = BayesianFactorFactory.factory().domain(dom).noisyOr(pars, trus, inhs);
						break;
					}
					case "NOT": {
						final int parent = popInteger();
						final int trueState = popInteger();

						factors[i] = BayesianFactorFactory.factory().domain(dom).not(parent, trueState);
						break;
					}

					default:
						throw new IllegalArgumentException("Unknown factor type: " + e);
				}
			}
		}
	}

}
