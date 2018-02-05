package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.GraphicalModel;
import gnu.trove.map.TIntIntMap;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public abstract class StochasticSampling {

	protected GraphicalModel<BayesianFactor> model;

	protected TIntIntMap evidence;

	protected long iterations = 1000;

	private Random random;
	private long seed = 42L;

	public StochasticSampling() {
		this.random = new Random(seed);
	}

	/**
	 * Set random seed (this re-initialize the random).
	 *
	 * @param seed new seed.
	 */
	public void setSeed(long seed) {
		this.seed = seed;
		random = new Random(seed);
	}

	/**
	 * Fix some evidence. The provided argument is a map of variable - state associations.
	 *
	 * @param evidence the map of observations
	 */
	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	/**
	 * Assign a new model and factors to this {@link StochasticSampling} algorithm.
	 *
	 * @param model the new model with factors
	 */
	public void setModel(GraphicalModel<BayesianFactor> model) {
		this.model = model.copy();
	}

	/**
	 * Default value is 1000.
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
	protected int sample(BayesianFactor factor) {
		double[] data = factor.getData();
		double p = random.nextDouble();

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
