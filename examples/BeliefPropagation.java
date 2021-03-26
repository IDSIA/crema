public class BeliefPropagation{

	public static void main(String[] args) {
		/* Define your Bayesian Network model */

		BayesianNetwork model = new BayesianNetwork();
		A = model.addVariable(2);
		B = model.addVariable(2);
		C = model.addVariable(2);

		model.addParent(B, A);
		model.addParent(C, A);

		// define the Bayesian Factors
		factors = new BayesianFactor[3];

		factors[A] = new BayesianFactor(model.getDomain(A));
		factors[B] = new BayesianFactor(model.getDomain(A, B));
		factors[C] = new BayesianFactor(model.getDomain(A, C));

		factors[A].setData(new int[]{A}, new double[]{.4, .6});
		factors[B].setData(new int[]{B, A}, new double[]{.3, .7, .7, .3});
		factors[C].setData(new int[]{C, A}, new double[]{.2, .8, .8, .2});

		// Assign factors to model
		model.setFactors(factors);

		// Instantiate the inference algorithm over BayesianFactors using the model

		BeliefPropagation<BayesianFactor> bp = new BeliefPropagation<>(model);

		// perform a full update
		BayesianFactor factor = bp.fullPropagation();

		// perform the distribution step
		bp.distributingEvidence();

		// perform the collection step
		BayesianFactor factor = bp.collectingEvidence();

		// Simple Inference

		// P(A)
		bp.clearEvidence(); //  this will clear previous evidence on the model

		BayesianFactor pA = bp.query(A);

		// Inference with evidence

		// P(A | B=0)
		TIntIntHashMap evidence = new TIntIntHashMap();
		evidence.put(B, 0);
		bp.setEvidence(evidence); // this will overwrite previous evidence

		BayesianFactor pAB0 = bp.query(A);

		// P(A | B=0, C=1)
		evidence = new TIntIntHashMap();
		evidence.put(B, 0);
		evidence.put(C, 1);
		bp.setEvidence(evidence);

		BayesianFactor bp.query(A);



	}
}