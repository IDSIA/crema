package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public class StochasticSamplingTest {

	private StochasticSampling ss;

	@Before
	public void setUp() {

		// This model is based on "Modeling and Reasoning with BN", Dawiche, p.378 and following
		SparseModel<BayesianFactor> model = new SparseModel<>();
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

		// Slipepry Road?
		int E = model.addVariable(2);
		model.addParent(E, C);
		f[E] = new BayesianFactor(model.getDomain(C, E), false);
		f[E].setData(new int[]{E, C}, new double[]{.7, .3, 0, 1});

		ss = new StochasticSampling(model, f);
		ss.setSeed(42);
	}

	@Test
	public void stochasticSample() {
		for (int i = 0; i < 10; i++) {
			TIntIntMap x = ss.simulateBN();
			System.out.println(x);
		}

		// TODO: complete with assertion
	}

	@Test
	public void stochasticSampleWithEvidence() {
		ss.setEvidence(new TIntIntHashMap(new int[]{0}, new int[]{1}));

		for (int i = 0; i < 10; i++) {
			TIntIntMap x = ss.simulateBN();
			System.out.println(x);

			assert (x.get(0) == 1);
		}

		// TODO: complete with assertion
	}
}