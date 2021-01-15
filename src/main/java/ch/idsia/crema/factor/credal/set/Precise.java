package ch.idsia.crema.factor.credal.set;

import ch.idsia.crema.core.Strides;

public class Precise extends AbstractSet {

	private double[] vertex;

	public Precise() {
	}

	public Precise(Strides domain, double[] vertex) {
		super(domain);
		this.vertex = vertex;
	}

	public void setVertex(double[] vertex) {
		this.vertex = vertex;
	}

	public double[] getVertex() {
		return vertex;
	}

	@Override
	public Precise copy() {
		double[] v = null;
		if (vertex != null) {
			v = vertex.clone();
		}

		return new Precise(getDomain(), v);
	}
}
