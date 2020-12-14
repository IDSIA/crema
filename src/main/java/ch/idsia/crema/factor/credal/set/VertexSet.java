package ch.idsia.crema.factor.credal.set;

import java.util.ArrayList;
import java.util.List;

public class VertexSet extends AbstractSet {

	private List<double[]> vertices;

	public VertexSet() {
		this.vertices = new ArrayList<>();
	}

	public VertexSet add(double[] vertex) {
		this.vertices.add(vertex);
		return this;
	}

	@Override
	public VertexSet copy() {
		return null;
	}
}
