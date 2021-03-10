package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;

public abstract class DiscreteEM extends ExpectationMaximization<BayesianFactor> {

	protected InferenceJoined<GraphicalModel<BayesianFactor>, BayesianFactor> getDefaultInference(int[] elimSeq) {
		return new InferenceJoined<>() {
			@Override
			public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int... queries) {
				final CutObserved<BayesianFactor> co = new CutObserved<>();
				GraphicalModel<BayesianFactor> coModel = co.execute(model, evidence);

				final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();
				GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, evidence, queries);
				rb.filter(elimSeq);

				FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(elimSeq);
				fve.setNormalize(true);

				return fve.query(infModel, evidence, queries);

			}

			@Override
			public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int query) {
				return query(model, evidence, new int[]{query});
			}
		};
	}
}





