package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.04.2021 14:41
 */
public class VertexFactorFactory {

	private final IntList combinations = new IntArrayList();
	private final List<double[]> vertices = new ArrayList<>();

	private Strides separatedDomain = Strides.empty();
	private Strides vertexDomain = Strides.empty();

	private VertexFactorFactory() {
	}

	public static VertexFactorFactory factory() {
		return new VertexFactorFactory();
	}

	public VertexFactorFactory separatedDomain(Strides domain) {
		separatedDomain = domain;
		return this;
	}

	public VertexFactorFactory vertexDomain(Strides domain) {
		vertexDomain = domain;
		return this;
	}

	public VertexFactorFactory domain(Strides left) {
		return domain(left, Strides.empty());
	}

	public VertexFactorFactory domain(Strides left, Strides right) {
		separatedDomain(right);
		vertexDomain(left);
		return this;
	}

	public VertexFactorFactory constraintFactor(SeparateHalfspaceFactor constrainsFactor) {
		separatedDomain = constrainsFactor.getSeparatingDomain();
		vertexDomain = constrainsFactor.getDataDomain();
		return this;
	}

	public VertexFactorFactory addVertices(List<double[]> vertices, IntList combinations) {
		final int n = vertices.size();
		if (n != combinations.size())
			throw new IllegalArgumentException("Different numbers of vertices and combinations: got " + n + " vertices and " + combinations.size() + " combinations");

		for (int i = 0; i < n; i++) {
			addVertex(vertices.get(i), combinations.get(i));
		}

		return this;
	}

	public VertexFactorFactory addVertex(double[] vertex, int... groupStates) {
		final int offset = separatedDomain.getOffset(groupStates);
		combinations.add(offset);
		vertices.add(vertex);
		return this;
	}

	public VertexFactor log() {
		// TODO
		throw new NotImplementedException();
		// return new VertexLogFactor(separatedDomain, vertexDomain, data);
	}

	public VertexFactor get() {
		return new VertexDefaultFactor(vertexDomain, separatedDomain, vertices, combinations);
	}

}
