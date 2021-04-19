package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:46
 */
// TODO: to be completed
public class VertexLogFactor extends VertexDefaultFactor {

	public VertexLogFactor(Strides separatedDomain, Strides vertexDomain, double[][][] data) {
		super(separatedDomain, vertexDomain, data);
	}

	@Override
	public VertexLogFactor combine(VertexFactor other) {
		return combine(other, VertexLogFactor::new, Double::sum);
	}

}
