package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexDefaultFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

public class BayesianToVertex implements Converter<BayesianFactor, VertexFactor> {

	@Override
	public VertexFactor apply(BayesianFactor factor, Integer var) {
		final Strides left = Strides.as(var, factor.getDomain().getCardinality(var));
		final Strides right = factor.getDomain().remove(var);

		final int[] vars = ArraysUtil.append(
				left.getVariables(),
				right.getVariables()
		);

		final IndexIterator it = factor.getDomain().getReorderedIterator(vars);
		final int states = factor.getDomain().getCardinality(var);

		final List<double[]> vertices = new ArrayList<>();
		final TIntList combinations = new TIntArrayList();

		for (int i = 0; i < right.getCombinations(); i++) {
			final double[] v = new double[states];
			for (int j = 0; j < states; j++) {
				v[j] = factor.getValueAt(it.next());
			}
			vertices.add(v);
			combinations.add(i);
		}

		return new VertexDefaultFactor(left, right, vertices, combinations);
	}

	@Override
	public Class<VertexFactor> getTargetClass() {
		return VertexFactor.class;
	}

	@Override
	public Class<BayesianFactor> getSourceClass() {
		return BayesianFactor.class;
	}
}
