package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.operations.vertex.Filter;
import ch.idsia.crema.factor.operations.vertex.Marginal;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 10:37
 */
public class ExtensiveVertexDefaultFactor extends ExtensiveVertexAbstractFactor {

	protected ArrayList<double[]> data;

	protected ExtensiveVertexDefaultFactor(Strides domain, int capacity) {
		super(domain);
		this.data = new ArrayList<>(capacity);
	}

	public ExtensiveVertexDefaultFactor(Strides domain, List<double[]> data) {
		this(domain, data.size());
		data.forEach(this::addVertex);
	}

	protected void addVertex(double[] vertex) {
		this.data.add(vertex);
	}

	@Override
	public ExtensiveVertexAbstractFactor copy() {
		ArrayList<double[]> new_data = new ArrayList<>(data.size());
		for (double[] vertex : data) {
			new_data.add(vertex.clone());
		}
		return new ExtensiveVertexDefaultFactor(domain, new_data);
	}

	@Override
	public BayesianFactor getBayesianVertex(int vertex) {
		return new BayesianDefaultFactor(domain, data.get(vertex));
	}

	@Override
	public int size() {
		return data.size();
	}

	/**
	 * @return a <u>copy</u> of the internal data
	 */
	public List<double[]> getInternalVertices() {
		final List<double[]> out = new ArrayList<>();
		data.forEach(d -> out.add(d.clone()));
		return out;
	}

	/**
	 * @param vertex the vertex to retrieve
	 * @return a <u>copy</u> of the given vertex
	 */
	@Override
	public double[] getVertex(int vertex) {
		return data.get(vertex).clone();
	}

	@Override
	public ExtensiveVertexAbstractFactor combine(ExtensiveVertexAbstractFactor other) {
		return combine(other, ExtensiveVertexDefaultFactor::new);
	}

	@Override
	public ExtensiveVertexAbstractFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Marginal(domain.getSizeAt(offset), domain.getStrideAt(offset)), ExtensiveVertexDefaultFactor::new);
	}

	@Override
	public ExtensiveVertexAbstractFactor filter(int variable, int state) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Filter(domain.getStrideAt(offset), state), ExtensiveVertexDefaultFactor::new);
	}

}
