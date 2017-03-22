package ch.idsia.crema.user.credal;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="set")
public class VertexSet  {
	
	private ArrayList<Vertex> vertices;

	public VertexSet() {
	}
	
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}
	
	public void setVertices(ArrayList<Vertex> vertices) {
		this.vertices = vertices;
	}
}
