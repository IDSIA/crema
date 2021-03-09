package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;

public abstract class DiscreteEM extends ExpectationMaximization<BayesianFactor> {

	protected Inference<GraphicalModel<BayesianFactor>, BayesianFactor> getDefaultInference(int[] elimSeq) {
		return (m, evidence, query) -> {
			final CutObserved<BayesianFactor> co = new CutObserved<>();
			GraphicalModel<BayesianFactor> coModel = co.execute(m, evidence);

			final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();
			GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, evidence, query);
			rb.filter(elimSeq);

			FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
			fve.setNormalize(true);

			return (BayesianFactor) fve.query(infModel, evidence, query);
		};
	}
}





