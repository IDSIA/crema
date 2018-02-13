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

	public void setCliques(Set<Clique> cliques) {
		this.cliques = cliques;
	}

	public Graph<Clique, DefaultWeightedEdge> getModel() {
		return model;
	}

	public Graph<Clique, DefaultWeightedEdge> exec() {

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
