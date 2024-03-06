package ch.idsia.crema.factor.credal.vertex.extensive;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.LogSpace;
import ch.idsia.crema.factor.algebra.collectors.Filter;
import ch.idsia.crema.factor.algebra.collectors.LogMarginal;
import ch.idsia.crema.factor.algebra.vertex.LogVertexOperation;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.utility.ArraysMath;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 10:38
 */
@LogSpace
public class ExtensiveVertexLogFactor extends ExtensiveVertexDefaultFactor {

	/**
	 * @param strides domain of the new factor
	 * @param data    list of vertices in non-log space. They will be converted in log-space by the method
	 */
	public ExtensiveVertexLogFactor(Strides strides, List<double[]> data) {
		super(strides, data.size());
		data.forEach(this::addVertex);
	}

	/**
	 * @param strides  domain of the new factor
	 * @param data     list of vertices <u>already in log-space</u>, no transformation will be made
	 * @param isLogged always true (we ignore it :P )
	 */
	ExtensiveVertexLogFactor(Strides strides, List<double[]> data, boolean isLogged) {
		super(strides, data.size());
		data.forEach(super::addVertex);
	}

	/**
	 * @param factor the given factor will be copied and converted in log-space
	 */
	public ExtensiveVertexLogFactor(ExtensiveVertexDefaultFactor factor) {
		super(factor.getDomain(), factor.size());
		factor.data.forEach(this::addVertex);
	}

	protected void addVertex(double[] data) {
		super.addVertex(ArraysMath.log(data));
	}

	@Override
	public BayesianFactor getBayesianVertex(int vertex) {
		return BayesianFactorFactory.factory().domain(getDomain()).logData(data.get(vertex)).log();
	}

	@Override
	public ExtensiveVertexLogFactor combine(ExtensiveVertexFactor other) {
		return combine(other, ExtensiveVertexLogFactor::new, new LogVertexOperation());
	}

	@Override
	public ExtensiveVertexLogFactor marginalize(int variable) {
		int offset = getDomain().indexOf(variable);
		return collect(offset, new LogMarginal(getDomain().getSizeAt(offset), getDomain().getStrideAt(offset)), ExtensiveVertexLogFactor::new);
	}

	@Override
	public ExtensiveVertexLogFactor filter(int variable, int state) {
		int offset = getDomain().indexOf(variable);
		return collect(offset, new Filter(getDomain().getStrideAt(offset), state), ExtensiveVertexLogFactor::new);
	}

}
