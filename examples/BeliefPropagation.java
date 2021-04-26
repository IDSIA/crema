package examples;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import gnu.trove.map.hash.TIntIntHashMap;

public class BeliefPropagation {

	public static void main(String[] args) {
		/* Define your Bayesian Network model */

		BayesianNetwork model = new BayesianNetwork();
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		model.addParent(B, A);
		model.addParent(C, A);

		// define the Bayesian Factors
		BayesianFactor[] factors = new BayesianFactor[3];

		factors[A] = BayesianFactorFactory.factory().domain(model.getDomain(A))
				.data(new int[]{A}, new double[]{.4, .6})
				.get();
		factors[B] = BayesianFactorFactory.factory().domain(model.getDomain(A, B))
				.data(new int[]{B, A}, new double[]{.3, .7, .7, .3})
				.get();
		factors[C] = BayesianFactorFactory.factory().domain(model.getDomain(A, C))
				.data(new int[]{C, A}, new double[]{.2, .8, .8, .2})
				.get();

		// Assign factors to model
		model.setFactors(factors);

		// Instantiate the inference algorithm over BayesianFactors using the model
		BayesianFactor factor;

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>();

		// perform a full update
		factor = bp.fullPropagation(model, A);

		// perform the distribution step
		bp.distributingEvidence();

		// perform the collection step
		factor = bp.collectingEvidence(A);

		// Simple Inference

		// P(A)
		BayesianFactor pA = bp.query(model, A);

		// Inference with evidence

		// P(A | B=0)
		TIntIntHashMap evidence = new TIntIntHashMap();
		evidence.put(B, 0);

		BayesianFactor pAB0 = bp.query(model, evidence, A);

		// P(A | B=0, C=1)
		evidence = new TIntIntHashMap();
		evidence.put(B, 0);
		evidence.put(C, 1);

		BayesianFactor pAB0C1 = bp.query(model, evidence, A);
	}
}