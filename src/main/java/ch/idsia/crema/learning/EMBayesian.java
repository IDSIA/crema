package ch.idsia.crema.learning;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.ProbabilityUtil;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Claudio Bonesana
 */
public class EMBayesian extends EMAlgorithm<BayesianFactor> {

	private int[] ignoreVariables = new int[0];
	private boolean smoothing = true;

	public EMBayesian(InferenceJoined<GraphicalModel<BayesianFactor>, BayesianFactor> inference) {
		super(inference);
	}

	// TODO:
	//  - add blocked states (state in variable will be copied from old factor)
	//  - add block variable (whole factor will be copied)

	public EMBayesian useBayesianSmoothing(boolean smoothing) {
		this.smoothing = smoothing;
		return this;
	}

	public EMBayesian setIgnoreVariables(TIntList ignoreVariables) {
		this.ignoreVariables = ignoreVariables.toArray();
		return this;
	}

	public EMBayesian setTrainableVars(int... trainableVars) {
		this.ignoreVariables = trainableVars;
		return this;
	}

	@Override
	public TIntObjectMap<BayesianFactor> expectation(GraphicalModel<BayesianFactor> model, TIntIntMap[] observations) {
		final TIntObjectMap<BayesianFactor> factors = new TIntObjectHashMap<>();
		final TIntObjectMap<double[]> counts = new TIntObjectHashMap<>();
		final int[] trainVariables = IntStream.of(model.getVariables())
				.filter(v -> !ArraysUtil.contains(v, ignoreVariables))
				.toArray();

		for (int v : trainVariables) {
			final int n = model.getFactor(v).getDomain().getCombinations();
			double[] data = new double[n];
			if (smoothing)
				Arrays.fill(data, 1.0 / n);
			counts.put(v, data);
		}
		for (int v : trainVariables) {
			factors.put(v, model.getFactor(v).copy());
		}

		int j =0 ;
		for (TIntIntMap observation : observations) {
			j++;
			for (int v : trainVariables) {
				final int[] targets = model.getFactor(v).getDomain().getVariables();
				long unknown = Arrays.stream(targets).filter(t -> !observation.containsKey(t)).count();

				if (unknown > 0) {
					// case with hidden data
					BayesianFactor inf = inference.query(model, observation, targets);

					final double[] data = counts.get(v);
					for (int i = 0; i < data.length; i++) {
						data[i] += inf.getValueAt(i);
					}

				} else {
					// fully-observed case
					final Strides dom = model.getFactor(v).getDomain();
					final int[] states = Arrays.stream(dom.getVariables()).map(observation::get).toArray();
					final int i = dom.getOffset(states);

					final double[] data = counts.get(v);
					data[i] += 1;
				}
			}
		}

		for (int v : trainVariables) {
			factors.put(v, new BayesianDefaultFactor(model.getFactor(v).getDomain(), counts.get(v)));
		}

		return factors;
	}

	@Override
	public GraphicalModel<BayesianFactor> maximization(GraphicalModel<BayesianFactor> model, TIntObjectMap<BayesianFactor> counts) {
		final GraphicalModel<BayesianFactor> copy = model.copy();

		final int[] trainVariables = IntStream.of(model.getVariables())
				.filter(v -> !ArraysUtil.contains(v, ignoreVariables))
				.toArray();

		for (int v : trainVariables) {
			final int[] parents = model.getParents(v);

			BayesianFactor f = counts.get(v);

			for (int p: parents)
				f = f.divide(counts.get(p));

			f = f.marginalize(v);
			copy.setFactor(v, f);
		}

		return copy;
	}

	/**
	 * Computes the log-likelihood of the model respec to the input observations.
	 *
	 * @param model        model learned during a {@link #step(GraphicalModel, TIntIntMap[])} operation
	 * @param observations data to learn from
	 * @return the log likelihood of the model respect to the input observations
	 */
	@Override
	public double score(GraphicalModel<BayesianFactor> model, TIntIntMap[] observations) {
		return ProbabilityUtil.logLikelihood(model, observations);
	}
}
