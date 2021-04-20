package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.factor.operations.vertex.Filter;
import ch.idsia.crema.factor.operations.vertex.LogMarginal;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 10:38
 */
public class ExtensiveVertexLogFactor extends ExtensiveVertexDefaultFactor {

	public ExtensiveVertexLogFactor(Strides strides, List<double[]> data) {
		super(strides, data.size());
		data.forEach(this::addVertex);
	}

	/**
	 * @param strides  domain
	 * @param data     given data should already be in log space.
	 * @param isLogged always true (we ignore it :P )
	 */
	ExtensiveVertexLogFactor(Strides strides, List<double[]> data, boolean isLogged) {
		super(strides, data.size());
		data.forEach(super::addVertex);
	}

	protected void addVertex(double[] data) {
		super.addVertex(ArraysUtil.log(data));
	}

	@Override
	public BayesianFactor getBayesianVertex(int vertex) {
		// TODO: this is stupid: we take an array in log-space, exp it, then log it again! Create a constructor for BayesLogFactor with a fake "isLogged" parameter
		return new BayesianLogFactor(domain, ArraysUtil.exp(data.get(vertex)));
	}

	@Override
	public ExtensiveVertexAbstractFactor combine(ExtensiveVertexAbstractFactor other) {
		return combine(other, ExtensiveVertexDefaultFactor::new);
	}

	@Override
	public ExtensiveVertexLogFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		return collect(offset, new LogMarginal(domain.getSizeAt(offset), domain.getStrideAt(offset)), ExtensiveVertexLogFactor::new);
	}

	@Override
	public ExtensiveVertexLogFactor filter(int variable, int state) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Filter(domain.getStrideAt(offset), state), ExtensiveVertexLogFactor::new);
	}

}
