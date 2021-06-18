package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.utility.RandomUtil;

import java.util.Random;

public class HalfspaceToRandomBayesian implements Converter<SeparateHalfspaceFactor, BayesianFactor> {

	private final Random random = RandomUtil.getRandom();

	@Override
	public BayesianFactor apply(SeparateHalfspaceFactor s, Integer var) {
		VertexFactor v = new HalfspaceToVertex().apply(s, var);

		BayesianFactorFactory bff = BayesianFactorFactory.factory().domain(v.getDomain());

		for (int i = 0; i < v.getSeparatingDomain().getSize(); i++) {
			final double[][] vertices = v.getVerticesAt(i);
			final int j = random.nextInt(vertices.length);
			final double[] vertex = vertices[j];

			// TODO: check if conversion works as intended
			for (int k = 0; k < vertex.length; k++) {
				bff.set(vertex[k], k, i);
			}
		}

		return bff.get();
	}

	@Override
	public Class<BayesianFactor> getTargetClass() {
		return BayesianFactor.class;
	}

	@Override
	public Class<SeparateHalfspaceFactor> getSourceClass() {
		return SeparateHalfspaceFactor.class;
	}



}
