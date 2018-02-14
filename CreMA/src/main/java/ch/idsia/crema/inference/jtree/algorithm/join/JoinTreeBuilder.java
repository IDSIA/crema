package ch.idsia.crema.inference.jtree.algorithm.join;

import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 09:29
 */
public abstract class JoinTreeBuilder {

	private Graph<Clique, DefaultWeightedEdge> model;
	private Graph<Clique, DefaultWeightedEdge> joinTree;

	/**
	 * @param model the model produced by a {@link JoinGraphBuilder}
	 */
	public void setModel(Graph<Clique, DefaultWeightedEdge> model) {
		this.model = model;
	}

	/**
	 * @return the last computed join tree
	 */
	public Graph<Clique, DefaultWeightedEdge> getJoinTree() {
		return joinTree;
	}

	/**
	 * Builds a join tree using the maximal spanning tree found in the model. The Type of algorithm to use is determined
	 * by the implementation of {@link #getMaximalSpanningTree(Graph)}.
	 *
	 * @return a graph containing the maximal spanning tree over the cliques of the original model
	 */
	public Graph<Clique, DefaultWeightedEdge> exec() {
		if (model == null) throw new IllegalArgumentException("No model available");

		joinTree = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// apply the maximal spanning tree algorithm
		SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> maximalSpanningTree = getMaximalSpanningTree(model);

		// add all the cliques from the original model to the join tree
		model.vertexSet().forEach(v -> joinTree.addVertex(v));

		// iterate over the maximal spanning tree and add all the edges to the join tree
		for (DefaultWeightedEdge edge : maximalSpanningTree) {
			Clique source = model.getEdgeSource(edge);
			Clique target = model.getEdgeTarget(edge);
			double weight = model.getEdgeWeight(edge);
			joinTree.addEdge(source, target, edge);
			joinTree.setEdgeWeight(edge, weight);
		}

		return joinTree;
	}

	/**
	 * Apply an algorithm that can create a maximal spanning tree over the input model
	 *
	 * @return the maximal spanning tree found
	 */
	abstract SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> getMaximalSpanningTree(Graph<Clique, DefaultWeightedEdge> model);
}
