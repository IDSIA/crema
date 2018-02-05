package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collection;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public class StochasticSamplingTest {

	SparseModel<BayesianFactor> model;

	@Before
	public void setUp() {
		// This model is based on "Modeling and Reasoning with BN", Dawiche, p.378 and following
		model = new SparseModel<>();
		BayesianFactor[] f = new BayesianFactor[5];

		// Winter?
		int A = model.addVariable(2);
		f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4}, false);

		// Sprinkler?
		int B = model.addVariable(2);
		model.addParent(B, A);
		f[B] = new BayesianFactor(model.getDomain(A, B), false);
		f[B].setData(new int[]{B, A}, new double[]{.2, .8, .75, .25});

		// Rain?
		int C = model.addVariable(2);
		model.addParent(C, A);
		f[C] = new BayesianFactor(model.getDomain(A, C), false);
		f[C].setData(new int[]{C, A}, new double[]{.8, .2, .1, .9});

		// Wet Grass?
		int D = model.addVariable(2);
		model.addParent(D, B);
		model.addParent(D, C);
		f[D] = new BayesianFactor(model.getDomain(B, C, D), false);
		f[D].setData(new int[]{D, B, C}, new double[]{.95, .05, .9, .1, .8, .2, 0, 1});

		// Slippery Road?
		int E = model.addVariable(2);
		model.addParent(E, C);
		f[E] = new BayesianFactor(model.getDomain(C, E), false);
		f[E].setData(new int[]{E, C}, new double[]{.7, .3, 0, 1});

		model.setFactors(f);
	}

	public String factorsToString(Collection<BayesianFactor> factors) {
		StringBuilder sb = new StringBuilder();
		for (BayesianFactor factor : factors) {
			sb.append(Arrays.toString(factor.getData()));
		}

		return sb.toString();
	}
}