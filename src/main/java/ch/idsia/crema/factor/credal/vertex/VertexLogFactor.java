package ch.idsia.crema.factor.credal.vertex;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:46
 */
public class VertexLogFactor extends VertexDefaultFactor {

	@Override
	public VertexLogFactor combine(VertexFactor other) {
		return combine(other, VertexLogFactor::new, Double::sum);
	}

}
