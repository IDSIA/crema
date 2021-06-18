package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactorFactory;

public class BayesianToExtensiveVertex implements Converter<BayesianFactor, ExtensiveVertexFactor> {
	public static final BayesianToExtensiveVertex INSTANCE = new BayesianToExtensiveVertex();

	@Override
	public ExtensiveVertexFactor apply(BayesianFactor cpt, Integer var) {
		final ExtensiveVertexFactorFactory evff = ExtensiveVertexFactorFactory.factory()
				.domain(cpt.getDomain())
				.addBayesVertex(cpt);

		if (cpt instanceof BayesianLogFactor)
			return evff.log();
		return evff.get();
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
