package examples;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.graphical.BayesianNetwork;


public class BayesianFactors {
	public static void main(String[] args) {

		// Create domain of 2 variables of sizes 2,3
		Domain d1 = DomainBuilder.var(0, 1).size(2, 3);

		// Some operations over a domain
		d1.getVariables();              // vector of variables (int[])
		d1.getSize();                   // number of variables
		d1.getCardinality(0);    // size of a specific variable
		d1.getSizeAt(0);


		// Crate a factor over the domain and set the data (i.e., values) of the factor
		BayesianFactor f = new BayesianDefaultFactor(d1,  // P([0, 1])  ->  P([X|Y])
				new double[]{
						// x1 x2
						0.2, 0.8, // y1
						0.5, 0.5, // y2
						0.1, 0.9  // y3
				});
		f.getDomain();

		// or using the dedicated Factory class:
		f = BayesianFactorFactory.factory()
				.domain(d1)
				.data(new double[]{0.2, 0.8, 0.5, 0.5, 0.1, 0.9})
				.get();

		f.marginalize(0).getData(); // = double[3] { 1.0, 1.0, 1.0 }


		////// A factor can also be defined in the context of a specific model


		// Define the BN
		BayesianNetwork model = new BayesianNetwork();
		model.addVariables(2, 3);
		model.addParent(0, 1);

		// extract a domain with varaiables 0 and 1 in the BN
		Domain d2 = model.getDomain(0, 1);

		// Create an empty factor with that domain
		f = new BayesianDefaultFactor(model.getDomain(0, 1), null);


	}
}
//53

