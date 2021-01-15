package ch.idsia.crema.factor.credal.vertex.algebra;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.math.Operation;

import java.util.Arrays;

public class DefaultSeparateAlgebra implements Operation<VertexFactor> {

	private double tollerance = 0.0000001;

	public DefaultSeparateAlgebra() {
	}

	public DefaultSeparateAlgebra(double tollerance) {
		this.tollerance = tollerance;
	}

	public VertexFactor round(VertexFactor factor) {
		double[][][] data = new double[factor.getSeparatingDomain().getCombinations()][][];

		for (int cindex = 0; cindex < data.length; ++cindex) {
			double[][] source_vertices = factor.getInternalData()[cindex];
			double[][] target_vertices = data[cindex] = new double[source_vertices.length][];

			for (int vindex = 0; vindex < source_vertices.length; ++vindex) {
				double[] source_vertex = source_vertices[vindex];
				double[] target_vertex = target_vertices[vindex] = new double[source_vertex.length];

				double sum = 0;
				// TODO: FIX!!!!!!!
				double total = Arrays.stream(target_vertex).sum();
				for (int param = 0; param < source_vertex.length - 1; ++param) {
					sum += target_vertex[param] = Math.round(source_vertex[param] / tollerance) * tollerance;
				}
				target_vertex[target_vertex.length - 1] = total - sum;
			}

		}
		return new VertexFactor(factor.getDataDomain(), factor.getSeparatingDomain(), data);
	}

	/**
	 * sum/marginalize some variables out of the credal set.
	 *
	 * @param vars
	 * @return
	 */
	@Override
	public VertexFactor marginalize(VertexFactor fact, int vars) {
		return fact.marginalize(vars);
	}

	@Override
	public VertexFactor combine(VertexFactor first, VertexFactor other) {
		return first.combine(other);
	}

	@Override
	public VertexFactor filter(VertexFactor factor, int variable, int state) {
		return factor.filter(variable, state);
	}

	@Override
	public VertexFactor divide(VertexFactor one, VertexFactor other) {
		return one.divide(other);
	}
}
