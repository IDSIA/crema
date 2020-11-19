package ch.idsia.crema.model.graphical;

import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Ints;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Set;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 10:08
 */
public class SparseDirectedAcyclicGraph extends DirectedAcyclicGraph<Integer, DefaultEdge> implements Graph {


	public SparseDirectedAcyclicGraph() {
		super(DefaultEdge.class);
	}

	@Override
	public void addVariable(int variable, int size) {
		super.addVertex(variable);
	}

	public void addVariable(int variable) {
		super.addVertex(variable);
	}

	public int addVariable(){
		int vid = IntStream.of(getVariables()).max().orElse(0) + 1;
		super.addVertex(vid);
		return vid;
	}

	@Override
	public void removeVariable(int variable) {
		super.removeVertex(variable);
	}

	@Override
	public void removeLink(int from, int to) {
		super.removeEdge(from, to);
	}

	@Override
	public void addLink(int from, int to) {
		super.addEdge(from, to);
	}

	@Override
	public int[] getParents(int variable) {
		Set<DefaultEdge> edges = super.incomingEdgesOf(variable);
		return edges.stream().mapToInt(super::getEdgeSource).sorted().toArray();
	}

	@Override
	public int[] getChildren(int variable) {
		Set<DefaultEdge> edges = super.outgoingEdgesOf(variable);
		return edges.stream().mapToInt(super::getEdgeTarget).sorted().toArray();
	}

	@Override
	public SparseDirectedAcyclicGraph copy() {
		final SparseDirectedAcyclicGraph copy = new SparseDirectedAcyclicGraph();

		super.vertexSet().forEach(v -> copy.addVariable(v, 0));
		super.edgeSet().forEach(e -> copy.addLink(super.getEdgeSource(e), super.getEdgeTarget(e)));

		return copy;
	}


	public int[] getVariables(){
		return Ints.toArray(this.vertexSet());
	}

	public boolean isLeaf(int variable){
		return getParents(variable).length==0;
	}

	public boolean isBarren(int variable){ return getChildren(variable).length==0; }


	public int[] markovBlanket(int v){

		int[] paCh = Ints.concat(IntStream.of(this.getChildren(v))
								.mapToObj(c -> getParents(c))
								.toArray(int[][]::new));

		paCh = IntStream.of(paCh).filter(x->x!=v).toArray();

		return ArraysUtil.unique(Ints.concat(this.getParents(v), this.getChildren(v), paCh));

	}



}
