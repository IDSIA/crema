package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;

public class BayesianToInterval implements Converter<BayesianFactor, IntervalFactor> {
	public static final BayesianToInterval INSTANCE = new BayesianToInterval();

	@Override
	public IntervalFactor apply(BayesianFactor cpt, Integer var) {
		return new VertexToInterval().apply(new BayesianToVertex().apply(cpt, var));
	}

	@Override
	public Class<IntervalFactor> getTargetClass() {
		return IntervalFactor.class;
	}

	@Override
	public Class<BayesianFactor> getSourceClass() {
		return BayesianFactor.class;
	}
}
