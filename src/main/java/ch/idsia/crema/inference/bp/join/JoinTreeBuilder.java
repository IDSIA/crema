package ch.idsia.crema.inference.bp.join;

import ch.idsia.crema.inference.Algorithm;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.cliques.CliqueSet;
import ch.idsia.crema.inference.bp.cliques.FindCliques;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 09:29
 */
public abstract class JoinTreeBuilder implements Algorithm<CliqueSet, JoinTree> {

	private CliqueSet cliques;
	private JoinTree joinTree;

	/**
	 * @param model the model produced by a {@link FindCliques}
	 */
	@Override
	public void setInput(CliqueSet model) {
		this.cliques = model;
	}

	/**
	 * @return the last computed join tree
	 */
	@Override
	public JoinTree getOutput() {
		return joinTree;
	}

	/**
	 * Builds a join tree using the maximal spanning tree found in the model. The Type of algorithm to use is determined
	 * by the implementation of {@link #getMaximalSpanningTree(JoinTree)}.
	 *
	 * @return a graph containing the maximal spanning tree over the cliques of the original model
	 */
	@Override
	public JoinTree exec() {
		if (cliques == null) throw new IllegalArgumentException("No model available");

		JoinTree slack = new JoinTree();

		// add new clique to graph
		cliques.forEach(slack::addVertex);

		for (Clique clique : cliques) {
			// search for other cliques with same variables
			for (Clique other : cliques) {
				if (clique.equals(other)) continue;

				// find how many variables are in common
				int[] intersection = clique.intersection(other);

				// if we have an intersection...
				if (intersection.length > 0) {
					DefaultWeightedEdge edge = slack.addEdge(clique, other);
					// ...and a new edge, add it to the model with the size of intersection as weight
					if (edge != null)
						slack.setEdgeWeight(edge, -intersection.length);
				}
			}
		}

		// apply the maximal spanning tree algorithm
		SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> maximalSpanningTree = getMaximalSpanningTree(slack);

		// add all the cliques from the original model to the join tree
		joinTree = new JoinTree();
		slack.vertexSet().forEach(v -> joinTree.addVertex(v));

		// iterate over the maximal spanning tree and add all the edges to the join tree
		for (DefaultWeightedEdge edge : maximalSpanningTree) {
			Clique source = joinTree.getEdgeSource(edge);
			Clique target = joinTree.getEdgeTarget(edge);
			double weight = joinTree.getEdgeWeight(edge);
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
	abstract SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> getMaximalSpanningTree(JoinTree model);
}
