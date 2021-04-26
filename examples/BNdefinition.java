package examples;

import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;


public class BNdefinition {
	public static void main(String[] args) {

		BayesianNetwork model = new BayesianNetwork();
		BayesianFactor[] f = new BayesianFactor[5];

		// Winter?
		int A = model.addVariable(2);
		f[A] = new BayesianDefaultFactor(model.getDomain(A), new double[]{.6, .4});

		// Sprinkler?
		int B = model.addVariable(2);
		model.addParent(B, A);
		f[B] = new BayesianDefaultFactor(model.getDomain(A, B), new int[]{B, A}, new double[]{.2, .8, .75, .25});

		// Rain?
		int C = model.addVariable(2);
		model.addParent(C, A);
		f[C] = new BayesianDefaultFactor(model.getDomain(A, C), new int[]{C, A}, new double[]{.8, .2, .1, .9});

		// Wet Grass?
		int D = model.addVariable(2);
		model.addParent(D, B);
		model.addParent(D, C);
		f[D] = new BayesianDefaultFactor(model.getDomain(B, C, D), new int[]{D, B, C}, new double[]{.95, .05, .9, .1, .8, .2, 0, 1});

		// Slippery Road?
		int E = model.addVariable(2);
		model.addParent(E, C);
		f[E] = new BayesianDefaultFactor(model.getDomain(C, E), new int[]{E, C}, new double[]{.7, .3, 0, 1});

		model.setFactors(f);

		System.out.println(model);

		for (int x : model.getVariables()) {
			for (int y : model.getChildren(x)) {
				System.out.print("(" + x + "-->" + y + ")");
			}
		}

	}
}
//54