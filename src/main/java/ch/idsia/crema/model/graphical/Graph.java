package ch.idsia.crema.model.graphical;

public interface Graph {

	public void addVariable(int variable, int size);
	
	public void removeVariable(int variable);
	
	public void removeLink(int from, int to);
	
	public void addLink(int from, int to);

	public int[] getParents(int variable);

	public int[] getChildren(int variable);

	public Graph copy();

}
