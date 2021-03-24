package ch.idsia.crema.inference.bp.junction;

import ch.idsia.crema.inference.bp.cliques.Clique;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 17:36
 */
public class JunctionTree extends SimpleWeightedGraph<Clique, DefaultEdge> {

	public JunctionTree() {
		super(DefaultEdge.class);
	}

}
