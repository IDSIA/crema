package ch.idsia.crema.factor.credal.set;

import ch.idsia.crema.core.Strides;
import org.apache.commons.lang3.ArrayUtils;

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
			v = ArrayUtils.clone(vertex);
		}

		return new Precise(getDomain(), v);
	}
}
