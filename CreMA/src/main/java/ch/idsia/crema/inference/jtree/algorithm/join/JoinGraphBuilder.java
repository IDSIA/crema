package ch.idsia.crema.inference.jtree.algorithm.join;

import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    13.02.2018 15:49
 */
public class JoinGraphBuilder {

	private Graph<Clique, DefaultWeightedEdge> model;

	private Set<Clique> cliques;

	/**
	 * @param cliques the cliques to work on
	 */
	public void setCliques(Set<Clique> cliques) {
		this.cliques = cliques;
	}

	/**
	 * @return the last computed join graph
	 */
	public Graph<Clique, DefaultWeightedEdge> getModel() {
		return model;
	}

	/**
	 * Builds a join graph over the cliques given in input using the {@link #setCliques(Set)} method.
	 *
	 * @return a join graph where the weights of the edges are the number of element in commons between two cliques
	 */
	public Graph<Clique, DefaultWeightedEdge> exec() {
		if (cliques == null) throw new IllegalArgumentException("No cliques available");

		model = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// add new clique to graph
		cliques.forEach(c -> model.addVertex(c));

		for (Clique clique : cliques) {
			// search for other cliques with same variables
			for (Clique other : cliques) {
				if (clique.equals(other)) continue;

				// find how many variables are in common
				int[] intersection = clique.intersection(other);

				// if we have an intersection...
				if (intersection.length > 0) {
					DefaultWeightedEdge edge = model.addEdge(clique, other);
					// ...and a new edge, add it to the model with the size of intersection as weight
					if (edge != null)
						model.setEdgeWeight(edge, intersection.length);
				}
			}
		}

		return model;
	}
}
