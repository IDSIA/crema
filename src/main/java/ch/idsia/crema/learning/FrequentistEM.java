package ch.idsia.crema.learning;

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

	protected void stepPrivate(Collection<TIntIntMap> stepArgs) {
		// E-stage
		TIntObjectMap<BayesianFactor> counts = expectation(stepArgs.toArray(TIntIntMap[]::new));
		// M-stage
		maximization(counts);
	}

	protected TIntObjectMap<BayesianFactor> expectation(TIntIntMap[] observations) {

		TIntObjectMap<BayesianFactor> counts = new TIntObjectHashMap<>();
		for (int variable : posteriorModel.getVariables()) {
			counts.put(variable, new BayesianFactor(posteriorModel.getFactor(variable).getDomain(), false));
		}

		for (TIntIntMap observation : observations) {
			for (int var : trainableVars) {
				int[] relevantVars = ArraysUtil.addToSortedArray(posteriorModel.getParents(var), var);
				int[] hidden = IntStream.of(relevantVars).filter(x -> !observation.containsKey(x)).toArray();
				int[] obsVars = IntStream.of(relevantVars).filter(x -> observation.containsKey(x)).toArray();

				if (hidden.length > 0) {
					// Case with missing data
					BayesianFactor phidden_obs = inferenceEngine.query(posteriorModel, observation, hidden);
					if (obsVars.length > 0)
						phidden_obs = phidden_obs.combine(
								BayesianFactor.getJoinDeterministic(posteriorModel.getDomain(obsVars), observation)
						);

					counts.put(var, counts.get(var).addition(phidden_obs));

//					TODO: what to do if NaN?
//					if (Double.isNaN(counts.get(var).getData()[0]))
//						System.out.println();

				} else {
					//fully-observable case
					for (int index : counts.get(var).getDomain().getCompatibleIndexes(observation)) {
						double x = counts.get(var).getValueAt(index) + 1;
						counts.get(var).setValueAt(x, index);
					}
				}
			}
		}

		return counts;
	}

	private void maximization(TIntObjectMap<BayesianFactor> counts) {
		updated = false;

		for (int var : trainableVars) {
			BayesianFactor countVar = counts.get(var);

			if (regularization > 0.0) {
				BayesianFactor reg = posteriorModel.getFactor(var).scalarMultiply(regularization);
				countVar = countVar.addition(reg);
			}

			BayesianFactor f = countVar.divide(countVar.marginalize(var));

			if (f.KLdivergence(posteriorModel.getFactor(var)) > klthreshold) {
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
