package ch.idsia.crema.inference.jtree;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 13:32
 */
// TODO: move into JTree or remove
public class UndirectedGraph extends SimpleGraph<Integer, DefaultEdge> {

	public UndirectedGraph() {
		super(DefaultEdge.class);
	}

	public UndirectedGraph copy() {
		UndirectedGraph copy = new UndirectedGraph();

		this.vertexSet().forEach(copy::addVertex);
		this.edgeSet().forEach(edge -> copy.addEdge(this.getEdgeSource(edge), this.getEdgeTarget(edge)));

		return copy;
	}
}
