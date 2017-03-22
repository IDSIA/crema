package ch.idsia.crema.factor.credal.set;

import java.util.ArrayList;

public class VertexSet extends AbstractSet {
	private ArrayList<double[]> vertices;
	
	public VertexSet() {
		this.vertices = new ArrayList<double[]>();
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
