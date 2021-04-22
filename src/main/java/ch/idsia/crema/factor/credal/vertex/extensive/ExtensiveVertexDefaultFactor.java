package ch.idsia.crema.factor.credal.vertex.extensive;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.collectors.Filter;
import ch.idsia.crema.factor.algebra.collectors.Marginal;
import ch.idsia.crema.factor.algebra.vertex.SimpleVertexOperation;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 10:37
 */
public class ExtensiveVertexDefaultFactor extends ExtensiveVertexAbstractFactor {

	protected ArrayList<double[]> data;

	/**
	 * Creates a new {@link ExtensiveVertexDefaultFactor} with a specified capacity but no assigned vertices.
	 *
	 * @param domain   domain of the factor
	 * @param capacity initial capacity of the internal vertex store
	 */
	protected ExtensiveVertexDefaultFactor(Strides domain, int capacity) {
		super(domain);
		this.data = new ArrayList<>(capacity);
	}

	/**
	 * @param domain the domain of the factor
	 * @param data   data not in log-space to add to the new factor
	 */
	public ExtensiveVertexDefaultFactor(Strides domain, List<double[]> data) {
		this(domain, data.size());
		data.forEach(this::addVertex);
	}

	protected void addVertex(double[] vertex) {
		this.data.add(vertex);
	}

	@Override
	public ExtensiveVertexDefaultFactor copy() {
		ArrayList<double[]> new_data = new ArrayList<>(data.size());
		for (double[] vertex : data) {
			new_data.add(ArrayUtils.clone(vertex));
		}
		return new ExtensiveVertexDefaultFactor(domain, new_data);
	}

	/**
	 * @param vertex vertex to get
	 * @return a new {@link BayesianDefaultFactor} made with the data from the given vertex
	 */
	@Override
	public BayesianFactor getBayesianVertex(int vertex) {
		return new BayesianDefaultFactor(domain, data.get(vertex));
	}

	/**
	 * @return the number of vertices assigned to this factor
	 */
	@Override
	public int size() {
		return data.size();
	}

	/**
	 * @return a <u>copy</u> of the internal data
	 */
	public List<double[]> getInternalVertices() {
		final List<double[]> out = new ArrayList<>();
		data.forEach(d -> out.add(ArrayUtils.clone(d)));
		return out;
	}

	/**
	 * @param vertex the vertex to retrieve
	 * @return a <u>copy</u> of the given vertex
	 */
	@Override
	public double[] getVertex(int vertex) {
		return ArrayUtils.clone(data.get(vertex));
	}

	@Override
	public ExtensiveVertexDefaultFactor combine(ExtensiveVertexFactor other) {
		return combine(other, ExtensiveVertexDefaultFactor::new, new SimpleVertexOperation());
	}

	@Override
	public ExtensiveVertexDefaultFactor marginalize(int variable) {
		int offset = getDomain().indexOf(variable);
		return collect(offset, new Marginal(getDomain().getSizeAt(offset), getDomain().getStrideAt(offset)), ExtensiveVertexDefaultFactor::new);
	}

	@Override
	public ExtensiveVertexDefaultFactor filter(int variable, int state) {
		int offset = getDomain().indexOf(variable);
		return collect(offset, new Filter(getDomain().getStrideAt(offset), state), ExtensiveVertexDefaultFactor::new);
	}

}
