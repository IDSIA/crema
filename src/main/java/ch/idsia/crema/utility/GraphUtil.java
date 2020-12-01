package ch.idsia.crema.utility;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    30.11.2020 18:10
 */
public class GraphUtil {

	/**
	 * Copy the vertices and the edges from the source {@link Graph} to the destination {@link Graph}.
	 *
	 * @param source      copy from this graph
	 * @param destination to this other graph
	 */
	public static void copy(Graph<Integer, DefaultEdge> source, Graph<Integer, DefaultEdge> destination) {
		source.vertexSet().forEach(destination::addVertex);
		source.edgeSet().forEach(e -> destination.addEdge(source.getEdgeSource(e), source.getEdgeTarget(e)));
	}
}
