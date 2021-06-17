package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;

import java.util.ArrayList;
import java.util.List;

public class SeparateVertexToExtensiveVertex implements Converter<VertexFactor, ExtensiveVertexFactor> {

	@Override
	public ExtensiveVertexFactor apply(VertexFactor s, Integer var) {
		double[][][] data = s.getData();
		int vertices = 1;

		Strides domain = s.getDomain();
		Strides conditioning_domain = s.getSeparatingDomain();

		int[] variable = new int[domain.getSize()];
		int[] data_vars = s.getDataDomain().getVariables();
		int[] cond_vars = conditioning_domain.getVariables();

		System.arraycopy(data_vars, 0, variable, 0, data_vars.length);
		System.arraycopy(cond_vars, 0, variable, data_vars.length, cond_vars.length);

		for (double[][] d : data) vertices *= d.length;

		List<BayesianLogFactor> factors = new ArrayList<>();

		for (int vertex = 0; vertex < vertices; ++vertex) {
			int reminder = vertex;
			double[] vdata = new double[domain.getCombinations()];

			for (int conditioning = 0; conditioning < conditioning_domain.getCombinations(); ++conditioning) {
				double[][] set = data[conditioning];
				int idx = reminder % set.length;
				reminder /= set.length;
				int len = set[idx].length;
				System.arraycopy(set[idx], 0, vdata, conditioning * len, len);
			}

			BayesianLogFactor vertex_factor = new BayesianLogFactor(domain, variable, vdata);
			factors.add(vertex_factor);
		}

		return ExtensiveVertexFactorFactory.factory()
				.domain(domain)
				.addLogVertices(factors)
				.log();
	}

	@Override
	public Class<ExtensiveVertexFactor> getTargetClass() {
		return ExtensiveVertexFactor.class;
	}

	@Override
	public Class<VertexFactor> getSourceClass() {
		return VertexFactor.class;
	}

}
