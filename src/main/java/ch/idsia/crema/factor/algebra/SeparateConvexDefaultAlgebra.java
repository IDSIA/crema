package ch.idsia.crema.factor.algebra;

import ch.idsia.crema.factor.credal.vertex.separate.VertexDefaultFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.utility.hull.LPConvexHull;

public class SeparateConvexDefaultAlgebra extends SeparateDefaultAlgebra {

	private boolean round = false;
	private boolean chull = true;
	private double precision = 0.0;

	public SeparateConvexDefaultAlgebra() {
	}

	public SeparateConvexDefaultAlgebra(boolean round, double precision, boolean chull) {
		this.round = round;
		this.chull = chull;
		this.precision = precision;
	}

	@Override
	public VertexFactor marginalize(VertexFactor fact, int vars) {
		VertexFactor factor = super.marginalize(fact, vars);
		if (round)
			factor = round(factor);
		if (chull)
			factor = convex(factor);
		return factor;
	}

	public VertexFactor convex(VertexFactor factor) {
		double[][][] data = new double[factor.size()][][];
		for (int ix = 0; ix < data.length; ++ix) {
			data[ix] = LPConvexHull.compute(factor.getVerticesAt(ix)); // TODO: data[ix] = LPConvexHull.compute(factor.getInternalData()[ix], true);
		}

		return new VertexDefaultFactor(factor.getDataDomain(), factor.getSeparatingDomain(), data);
	}

	public VertexFactor fullConvex(VertexFactor factor) {
		double[][][] data = new double[factor.size()][][];
		for (int ix = 0; ix < data.length; ++ix) {
			data[ix] = LPConvexHull.compute(factor.getVerticesAt(ix)); // TODO: data[ix] = LPConvexHull.compute(factor.getInternalData()[ix], false);
		}

		return new VertexDefaultFactor(factor.getDataDomain(), factor.getSeparatingDomain(), data);
	}
}
