package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.ExtensiveVertexFactor;

public class BayesianToExtensiveVertex implements Converter<BayesianFactor, ExtensiveVertexFactor> {
	public static final BayesianToExtensiveVertex INSTANCE = new BayesianToExtensiveVertex();

	@Override
	public ExtensiveVertexFactor apply(BayesianFactor cpt, Integer var) {
		ExtensiveVertexFactor factor = new ExtensiveVertexFactor(cpt.getDomain(), cpt.isLog());
		factor.addInternalVertex(cpt.getInteralData());
		return factor;
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
