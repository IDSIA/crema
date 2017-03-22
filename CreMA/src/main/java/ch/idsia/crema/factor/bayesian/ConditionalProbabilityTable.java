package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.model.Domain;
import ch.idsia.crema.model.Strides;

public class ConditionalProbabilityTable extends BayesianFactor {

	public ConditionalProbabilityTable(Domain left, Domain given, boolean log) {
		super(null, false);
		Strides left2 = Strides.fromDomain(left).sort().union(Strides.fromDomain(given).sort());
		// TODO Auto-generated constructor stub
		
		ciao: for (int y = 0; y < 1000; ++y) {
			for (int x = 0; x < 100; ++x) {
				 break ciao;
			}
		}
		
		
	}

}
