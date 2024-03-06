package ch.idsia.crema.learning;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianDeterministicFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collection;
import java.util.stream.IntStream;

import static ch.idsia.crema.factor.bayesian.BayesianFactorUtilities.KLDivergence;

public class FrequentistEM extends DiscreteEM {

	private double regularization = 0.00001;

	public FrequentistEM(GraphicalModel<BayesianFactor> model, InferenceJoined<GraphicalModel<BayesianFactor>, BayesianFactor> inferenceEngine) {
		this.inferenceEngine = inferenceEngine;
		this.priorModel = model;
	}

	public FrequentistEM(GraphicalModel<BayesianFactor> model, int[] elimSeq) {
		this.inferenceEngine = getDefaultInference(elimSeq);
		this.priorModel = model;
	}

	public FrequentistEM(GraphicalModel<BayesianFactor> model) {
		this(model, (new MinFillOrdering()).apply(model));
	}

	@Override
	protected void stepPrivate(Collection<Int2IntMap> stepArgs) {
		// E-stage
		Int2ObjectMap<BayesianDefaultFactor> counts = expectation(stepArgs.toArray(Int2IntMap[]::new));
		// M-stage
		maximization(counts);
	}

	private Int2ObjectMap<BayesianDefaultFactor> expectation(Int2IntMap[] observations) {
		Int2ObjectMap<BayesianDefaultFactor> factors = new Int2ObjectOpenHashMap<>();
		Int2ObjectMap<double[]> counts = new Int2ObjectOpenHashMap<>();
		Int2ObjectMap<Strides> domains = new Int2ObjectOpenHashMap<>();
		for (int variable : posteriorModel.getVariables()) {
			final Strides domain = posteriorModel.getFactor(variable).getDomain();
			domains.put(variable, domain);
			counts.put(variable, new double[domain.getCombinations()]);
		}

		for (Int2IntMap observation : observations) {
			for (int var : trainableVars) {
				int[] relevantVars = ArraysUtil.addToSortedArray(posteriorModel.getParents(var), var);
				int[] hidden = IntStream.of(relevantVars).filter(x -> !observation.containsKey(x)).toArray();
				int[] obsVars = IntStream.of(relevantVars).filter(x -> observation.containsKey(x)).toArray();

				if (hidden.length > 0) {
					// Case with missing data
					BayesianFactor phidden_obs = inferenceEngine.query(posteriorModel, observation, hidden);
					if (obsVars.length > 0)
						phidden_obs = phidden_obs.combine(
								BayesianDeterministicFactor.getJoinDeterministic(posteriorModel.getDomain(obsVars), observation)
						);

					double[] data = counts.get(var);
					for (int i = 0; i < data.length; i++) {
						data[i] = data[i] + phidden_obs.getValueAt(i);
					}

//					TODO: what to do if NaN?
//					if (Double.isNaN(counts.get(var).getData()[0]))
//						System.out.println();

				} else {
					// fully-observable case
					for (int index : domains.get(var).getCompatibleIndexes(observation)) {
						final double[] data = counts.get(var);
						data[index] = data[index] + 1;
					}
				}
			}
		}

		// build output factors
		for (int v : domains.keySet()) {
			factors.put(v, new BayesianDefaultFactor(domains.get(v), counts.get(v)));
		}

		return factors;
	}

	private void maximization(Int2ObjectMap<BayesianDefaultFactor> counts) {
		updated = false;

		for (int var : trainableVars) {
			BayesianDefaultFactor countVar = counts.get(var);

			if (regularization > 0.0) {
				BayesianDefaultFactor reg = (BayesianDefaultFactor) posteriorModel.getFactor(var);
				countVar = countVar.addition(reg.scale(regularization));
			}

			BayesianFactor f = countVar.divide(countVar.marginalize(var));

			if (KLDivergence(f, posteriorModel.getFactor(var)) > klthreshold) {
				posteriorModel.setFactor(var, f);
				updated = true;
			}
		}
	}

	public FrequentistEM setRegularization(double regularization) {
		this.regularization = regularization;
		return this;
	}

	public double getRegularization() {
		return regularization;
	}

}
