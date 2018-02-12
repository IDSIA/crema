package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 10:57
 */
public class BayesianNetwork extends GenericSparseModel<BayesianFactor, SparseDirectedAcyclicGraph> {
	
	/**
	 * Create the directed model using the specified network implementation.
	 */
	public BayesianNetwork() {
		super(new SparseDirectedAcyclicGraph());
	}
}
