package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public abstract class StochasticSampling {

	protected BayesianNetwork model;

	protected TIntIntMap evidence;

	protected long iterations = 100;

	protected Boolean preprocess = true;

	public void setPreprocess(Boolean preprocess) {
		this.preprocess = preprocess;
	}

	protected BayesianNetwork preprocess(BayesianNetwork original, TIntIntMap evidence, int... query) {
		BayesianNetwork model = original;
		if (preprocess) {
			model = (BayesianNetwork) original.copy();
			final CutObserved<BayesianFactor> co = new CutObserved<>();
			final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();

			co.executeInPlace(model, evidence);
			rb.executeInPlace(model, evidence, query);
		}

		return model;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setModel(BayesianNetwork model) {
		this.model = (BayesianNetwork) model.copy();
	}

	/**
	 * Default value is 100.
	 *
	 * @param iterations number of iterations to do during the sampling
	 */
	public void setIterations(long iterations) {
		this.iterations = iterations;
	}

	/**
	 * Sample the distribution of a factor. Note that this factor distribution is over a single variable!
	 *
	 * @param factor factor to sample
	 * @return the index of sampled state
	 */
	// TODO: this should not be in an inference package
	protected int sample(BayesianFactor factor) {
		double[] data = factor.getData();
		double p = RandomUtil.getRandom().nextDouble();

		double sum = 0.0;

		int i = 0;
		for (; i < data.length; i++) {
			sum += data[i];
			if (sum > p)
				break;
		}

		return i;
	}
}
