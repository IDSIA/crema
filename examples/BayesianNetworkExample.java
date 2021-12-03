package example;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.graphical.BayesianNetwork;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.10.2021 11:33
 */
public class BayesianNetworkExample {
	public static void main(String[] args) {

		// [1] model declaration
		BayesianNetwork model = new BayesianNetwork();

		// variables declaration
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		// parents assignments
		model.addParent(C, A);
		model.addParent(C, B);

		// [2] domains definitions
		Domain domA = model.getDomain(A);
		Domain domB = model.getDomain(B);
		Domain domC = model.getDomain(C, A, B);

		// [3] factor definition
		BayesianFactor[] factors = new BayesianFactor[3];

		factors[A] = new BayesianDefaultFactor(domA, new double[]{.8, .2});

		factors[B] = BayesianFactorFactory.factory().domain(domA)
				.set(.4, 0)
				.set(.6, 1)
				.get();

		factors[C] = BayesianFactorFactory.factory().domain(domC)
				.set(.3, 0, 0, 0)
				.set(.7, 0, 0, 1)
				.set(.5, 0, 1, 0)
				.set(.5, 0, 1, 1)
				.set(.4, 1, 0, 0)
				.set(.6, 1, 0, 1)
				.set(.6, 1, 1, 0)
				.set(.4, 1, 1, 1)
				.get();

		// factor assignment
		model.setFactors(factors);

		// [4] end
	}
}
