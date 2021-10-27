package examples;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.hash.TIntIntHashMap;

public class BeliefPropagationExample {

	public static void main(String[] args) {

		// Define your Bayesian Network model
		BayesianNetwork model = new BayesianNetwork();
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		model.addParent(B, A);
		model.addParent(C, A);

		// Define the Bayesian Factors
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

		// [p1] Perform a query using the inference interface
		Inference<DAGModel<BayesianFactor>, BayesianFactor> inf = new BeliefPropagation<>();
		BayesianFactor factor = inf.query(model, A);

		// [p2] Simple Inference
		// P(A)
		BayesianFactor pA = inf.query(model, A);

		// [p3] Inference with evidence
		// P(A | B=0)
		TIntIntHashMap evidence = new TIntIntHashMap();
		evidence.put(B, 0);

		BayesianFactor pAb0 = inf.query(model, evidence, A);

		// P(A | B=0, C=1)
		evidence = new TIntIntHashMap();
		evidence.put(B, 0);
		evidence.put(C, 1);

		BayesianFactor pAb0c1 = inf.query(model, evidence, A);

		// [p4] Instantiate the inference algorithm over BayesianFactors using the model
		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>();

		// [p5] Perform a full update
		factor = bp.fullPropagation(model, A);

		// Perform the distribution step
		bp.distributingEvidence();

		// Perform the collection step
		factor = bp.collectingEvidence(A);
		// [p6] end
	}
}