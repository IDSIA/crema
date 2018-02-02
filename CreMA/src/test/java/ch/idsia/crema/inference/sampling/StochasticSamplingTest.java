package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.map.TIntIntMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public class StochasticSamplingTest {

	SparseModel<BayesianFactor> model;
	BayesianFactor[] f;

	@Before
	public void setUp() throws Exception {

		// This model is based on "Modeling and Reasoning with BN", Dawiche, p.378 and following
		model = new SparseModel<>();
		f = new BayesianFactor[5];

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

		// Slipepry Road?
		int E = model.addVariable(2);
		model.addParent(E, C);
		f[E] = new BayesianFactor(model.getDomain(C, E), false);
		f[E].setData(new int[]{E, C}, new double[]{.7, .3, 0, 1});
	}

	@Test
	public void stochasticSample() {
		StochasticSampling ss = new StochasticSampling(model, f);
		ss.setSeed(42);

		for (int i = 0; i < 10; i++) {
			TIntIntMap x = ss.simulateBN();

			System.out.println(x);
		}
	}
}