package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.JoinInference;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;

public abstract class DiscreteEM extends ExpectationMaximization<BayesianFactor> {

	protected JoinInference<BayesianFactor, BayesianFactor> getDefaultInference(GraphicalModel<BayesianFactor> model, int[] elimSeq) {
		return (m, query, obs) ->
		{
			CutObserved co = new CutObserved();
			GraphicalModel<BayesianFactor> coModel = co.execute(m, obs);

			RemoveBarren rb = new RemoveBarren();
			GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, query, obs);
			rb.filter(elimSeq);

			FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
			fve.setNormalize(true);

			return (BayesianFactor) fve.apply(infModel, query, obs);
		};
	}
}





