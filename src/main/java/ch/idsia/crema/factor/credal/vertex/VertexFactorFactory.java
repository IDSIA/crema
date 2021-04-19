package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.04.2021 14:41
 */
public class VertexFactorFactory {

	private TIntObjectMap<Set<double[]>> vertices = new TIntObjectHashMap<>();

	private boolean log = false;

	private Strides separatedDomain = Strides.empty();
	private Strides vertexDomain = Strides.empty();

	private VertexFactorFactory() {
	}

	public static VertexFactorFactory factory() {
		return new VertexFactorFactory();
	}

	public VertexFactorFactory log() {
		log = true;
		return this;
	}

	public VertexFactorFactory separatedDomain(Strides domain) {
		separatedDomain = domain;
		return this;
	}

	public VertexFactorFactory vertexDomain(Strides domain) {
		vertexDomain = domain;
		return this;
	}

	public VertexFactorFactory constraintFactor(SeparateHalfspaceFactor constrainsFactor) {
		separatedDomain = constrainsFactor.getSeparatingDomain();
		vertexDomain = constrainsFactor.getDataDomain();
		return this;
	}

	public VertexFactorFactory addVertices(List<double[]> vertices, TIntList combinations) {
		final int n = vertices.size();
		if (n != combinations.size())
			throw new IllegalArgumentException("Different numbers of vertices and combinations: got " + n + " vertices and " + combinations.size() + " combinations");

		for (int i = 0; i < n; i++) {
			addVertex(vertices.get(i), combinations.get(i));
		}

		return this;
	}

	public VertexFactorFactory addVertex(double[] vertex, int... groupStates) {
		int offset = separatedDomain.getOffset(groupStates);

		if (!vertices.containsKey(offset))
			vertices.put(offset, new HashSet<>());

		vertices.get(offset).add(vertex);
		return this;
	}

	public VertexFactor build() {
		final int n = separatedDomain.getCombinations();
		double[][][] data = new double[n][][];

		for (int offset : vertices.keys()) {
			for (double[] vertex : vertices.get(offset)) {
				double[][] gdata = data[offset];

				if (gdata != null) {
					// not first node
					int len = gdata.length;
					double[][] newdata = Arrays.copyOf(gdata, len + 1);
					newdata[len] = vertex;
					data[offset] = newdata;
				} else {
					// first node
					data[offset] = new double[][]{vertex};
				}
			}
		}

		if (log)
			return new VertexLogFactor(separatedDomain, vertexDomain, data);
		return new VertexDefaultFactor(separatedDomain, vertexDomain, data);
	}

}
