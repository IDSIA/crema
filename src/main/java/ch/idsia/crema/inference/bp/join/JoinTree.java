package ch.idsia.crema.inference.bp.join;

import ch.idsia.crema.inference.bp.cliques.Clique;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 17:26
 */
public class JoinTree extends SimpleWeightedGraph<Clique, DefaultWeightedEdge> {

	public JoinTree() {
		super(DefaultWeightedEdge.class);
	}
}
