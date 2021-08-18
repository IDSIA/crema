package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.08.2021 13:44
 */
public class VertexToRandomBayesian implements Converter<VertexFactor, BayesianFactor> {

	private final Random random = RandomUtil.getRandom();

	private boolean log = false;

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	@Override
	public BayesianFactor apply(VertexFactor v, Integer var) {
		final BayesianFactorFactory bff = BayesianFactorFactory.factory().domain(v.getDomain());

		final int[] vars = ArraysUtil.append(
				v.getDataDomain().getVariables(),
				v.getSeparatingDomain().getVariables()
		);

		// visiting BayesianFactor domain in a modified order
		final IndexIterator targetIterator = v.getDomain().getReorderedIterator(vars);

		for (int i = 0; i < v.getSeparatingDomain().getCombinations(); i++) {
			final double[][] vertices = v.getVerticesAt(i);
			final int j = random.nextInt(vertices.length);
			final double[] vertex = vertices[j];

			for (double value : vertex) {
				final int offset = targetIterator.next();
				bff.valueAt(value, offset);
			}
		}

		if (log) {
			return bff.log();
		} else {
			return bff.get();
		}
	}

	@Override
	public Class<BayesianFactor> getTargetClass() {
		return BayesianFactor.class;
	}

	@Override
	public Class<VertexFactor> getSourceClass() {
		return VertexFactor.class;
	}

}
