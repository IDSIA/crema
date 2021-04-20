package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.factor.credal.vertex.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.ExtensiveVertexFactorFactory;

public class BayesianToExtensiveVertex implements Converter<BayesianFactor, ExtensiveVertexFactor> {
	public static final BayesianToExtensiveVertex INSTANCE = new BayesianToExtensiveVertex();

	@Override
	public ExtensiveVertexFactor apply(BayesianFactor cpt, Integer var) {
		final boolean log = cpt instanceof BayesianLogFactor;

		return ExtensiveVertexFactorFactory.factory()
				.log(log)
				.domain(cpt.getDomain())
				.addBayesVertex(cpt)
				.build();
	}

	@Override
	public Class<ExtensiveVertexFactor> getTargetClass() {
		return ExtensiveVertexFactor.class;
	}

	@Override
	public Class<BayesianFactor> getSourceClass() {
		return BayesianFactor.class;
	}
}
