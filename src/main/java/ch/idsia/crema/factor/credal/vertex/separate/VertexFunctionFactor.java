package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.hull.ConvexHull;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Function;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:49
 */
public class VertexFunctionFactor extends VertexAbstractFactor {

	private final Function<Integer, double[][]> f;

	public VertexFunctionFactor(Strides separatedDomain, Strides vertexDomain, Function<Integer, double[][]> f) {
		super(vertexDomain, separatedDomain);
		this.f = f;
	}

	@Override
	protected void applyConvexHull(ConvexHull m) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFunctionFactor copy() {
		return new VertexFunctionFactor(separatedDomain, vertexDomain, f);
	}

	@Override
	public VertexFunctionFactor filter(int variable, int state) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFunctionFactor combine(VertexFactor other) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFunctionFactor normalize(int... given) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFunctionFactor getSingleVertexFactor(int... idx) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public int size() {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFunctionFactor reseparate(Strides target) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public double[][] getVertices(int... states) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public double[][] getVerticesAt(int i) {
		return f.apply(i);
	}

	@Override
	public double[][][] getData() {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFactor convexHull(ConvexHull m) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFactor merge(VertexFactor factor) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexFactor merge(VertexFactor... factors) {
		// TODO
		throw new NotImplementedException();
	}

}
