package ch.idsia.crema.preprocess.mergers;

import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;

import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.04.2021 14:00
 */
public class MergeFactorsInterval implements MergeFactors<IntervalFactor> {

	@Override
	public IntervalFactor merge(GraphicalModel<IntervalFactor> model, Int2IntMap evidence, int x1, int x2, int x, int y) {
		final IntervalFactor f1 = model.getFactor(x1);
		final IntervalFactor f2 = model.getFactor(x2);

		final int ev1 = evidence.get(x1);
		final int ev2 = evidence.get(x2);

		final IntervalFactorFactory iff = IntervalFactorFactory.factory().domain(model.getDomain(x), model.getDomain(y));

		// for each state of y
		for (int yi = 0; yi < model.getSize(y); yi++) {
			// the lower and upper probability of the same observed state is the product of the lowers and uppers of the observed states of the two nodes
			double lower_xt_y = f1.getLower(yi)[ev1] * f2.getLower(yi)[ev2];
			double upper_xt_y = f1.getUpper(yi)[ev1] * f2.getUpper(yi)[ev2];

			double[] lower_x_y = new double[]{lower_xt_y, 1.0 - upper_xt_y};
			double[] upper_x_y = new double[]{upper_xt_y, 1.0 - lower_xt_y};

			iff.lower(lower_x_y, yi);
			iff.upper(upper_x_y, yi);
		}

		return iff.get();
	}

}
