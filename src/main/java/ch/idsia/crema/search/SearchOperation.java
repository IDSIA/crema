package ch.idsia.crema.search;

public class SearchOperation {
	
	public void opening(int node, int from) {
	}
	
	public void closing(int node, int from) {
	}
	
	public boolean canVisit(int node, int from) {
		return true;
	}
	
	public boolean visitChildren(int node) {
		return true;
	}
	
	public boolean visitParents(int node) {
		return true;
	}
}
