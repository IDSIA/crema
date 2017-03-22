package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Converter;
import ch.idsia.crema.model.Strides;

public class SeparateVertexToExtensiveVertex implements Converter<VertexFactor, ExtensiveVertexFactor>{

	@Override
	public ExtensiveVertexFactor apply(VertexFactor s, Integer var) {
		double[][][] data = s.getData();
		int vertices = 1;
		
		Strides domain = s.getDomain();
		Strides conditioning_domain = s.getSeparatingDomain();
		
		ExtensiveVertexFactor factor = new ExtensiveVertexFactor(domain, true);
		
		int[] variable = new int[domain.getSize()];
		int[] data_vars = s.getDataDomain().getVariables();
		int[] cond_vars = conditioning_domain.getVariables();
		
		System.arraycopy(data_vars, 0, variable, 0, data_vars.length);
		System.arraycopy(cond_vars, 0, variable, data_vars.length, cond_vars.length);
		
		for (double[][] d : data) vertices *= d.length;
		
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
			
			BayesianFactor vertex_factor = new BayesianFactor(domain, true);
			vertex_factor.setData(variable, vdata);
			factor.addVertex(vertex_factor);	
		}
		return factor;
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
