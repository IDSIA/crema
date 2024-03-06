package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.LogSpace;
import ch.idsia.crema.utility.ArraysMath;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:46
 */
// TODO: to be completed
@LogSpace
public class VertexLogFactor extends VertexDefaultFactor {

	public VertexLogFactor(Strides vertexDomain, Strides separatedDomain, double[][][] data) {
		super(vertexDomain, separatedDomain, data);
	}

	public VertexLogFactor(VertexDefaultFactor factor) {
		super(factor.vertexDomain, factor.separatedDomain, new double[factor.data.length][][]);

		for (int i = 0; i < factor.data.length; i++) {
			data[i] = new double[factor.data[i].length][];
			for (int j = 0; j < factor.data[i].length; j++) {
				data[i][j] = ArraysMath.log(factor.data[i][j]);
			}
		}
	}

	@Override
	public VertexLogFactor combine(VertexFactor other) {
		// return combine(other, VertexLogFactor::new, Double::sum);
		// TODO:
		throw new NotImplementedException();
	}

}
