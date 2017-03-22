package ch.idsia.crema.user.credal;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="separate-factor")
public class VertexFactor {
	
	private ArrayList<VertexSet> sets;
	
	public VertexFactor() {
		// TODO Auto-generated constructor stub
	}
	
	public VertexFactor(ch.idsia.crema.factor.credal.vertex.VertexFactor factor) {
		
	}
	
	public ArrayList<VertexSet> getSets() {
		return sets;
	}
	
	public void setSets(ArrayList<VertexSet> sets) {
		this.sets = sets;
	}
}
