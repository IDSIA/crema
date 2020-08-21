package ch.idsia.crema.model.graphical.specialized;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GenericSparseModel;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 10:57
 * <p>
 * A BayesianNetwork is a special type of {@link GenericSparseModel}, composed with {@link BayesianFactor} and
 * constructed on a {@link SparseDirectedAcyclicGraph}.
 */
public class BayesianNetwork extends GenericSparseModel<BayesianFactor, SparseDirectedAcyclicGraph> {

	/**
	 * Create the directed model using the specified network implementation.
	 */
	public BayesianNetwork() {
		super(new SparseDirectedAcyclicGraph());
	}
	
	public BayesianNetwork copy(){

		BayesianNetwork copy = new BayesianNetwork();
		copy.addVariables(this.getSizes(this.getVariables()));
		for (int v : copy.getVariables()){
			copy.addParents(v, this.getParents(v));
			copy.setFactor(v, this.getFactor(v).copy());
		}

		return copy;
	}





}
