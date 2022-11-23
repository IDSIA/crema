package ch.idsia.crema.learning;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianDeterministicFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
	protected void stepPrivate(Collection<TIntIntMap> stepArgs) {
		// E-stage
		TIntObjectMap<BayesianDefaultFactor> counts = expectation(stepArgs.toArray(TIntIntMap[]::new));
		// M-stage
		maximization(counts);
	}

	private TIntObjectMap<BayesianDefaultFactor> expectation(TIntIntMap[] observations) {
		TIntObjectMap<BayesianDefaultFactor> factors = new TIntObjectHashMap<>();
		TIntObjectMap<double[]> counts = new TIntObjectHashMap<>();
		TIntObjectMap<Strides> domains = new TIntObjectHashMap<>();
		for (int variable : posteriorModel.getVariables()) {
			final Strides domain = posteriorModel.getFactor(variable).getDomain();
			domains.put(variable, domain);
			counts.put(variable, new double[domain.getCombinations()]);
		}

		for (TIntIntMap observation : observations) {
			for (int var : trainableVars) {
				int[] relevantVars = ArraysUtil.add(posteriorModel.getParents(var), var);
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
		for (int v : domains.keys()) {
			factors.put(v, new BayesianDefaultFactor(domains.get(v), counts.get(v)));
		}

		return factors;
	}

	private void maximization(TIntObjectMap<BayesianDefaultFactor> counts) {
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
