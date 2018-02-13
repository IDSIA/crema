package ch.idsia.crema.model.graphical;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Set;

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
}
