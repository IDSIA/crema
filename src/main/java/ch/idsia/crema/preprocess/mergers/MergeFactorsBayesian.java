package ch.idsia.crema.preprocess.mergers;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.04.2021 14:25
 */
public class MergeFactorsBayesian implements MergeFactors<BayesianFactor> {

	@Override
	public BayesianFactor merge(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int x1, int x2, int x, int y) {
		final BayesianFactor f1 = model.getFactor(x1);
		final BayesianFactor f2 = model.getFactor(x2);

		final int ev1 = evidence.get(x1);
		final int ev2 = evidence.get(x2);

		final BayesianFactorFactory bff = BayesianFactorFactory.factory().domain(model.getDomain(y, x));

		// for each state of y
		for (int yi = 0; yi < model.getSize(y); yi++) {
			// TODO: verify that this is the correct merge of two BayesianFactors!
			double p_xt_y = f1.getValue(yi, ev1) * f2.getValue(yi, ev2);

			bff.value(p_xt_y, yi, 0);
			bff.value(1.0 - p_xt_y, yi, 1);
		}

		return bff.get();
	}

}
