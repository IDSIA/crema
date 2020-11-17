package ch.idsia.crema.inference.jtree.algorithm.junction;

import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 17:36
 */
public class JunctionTree extends SimpleWeightedGraph<Clique, Separator> {

	public JunctionTree() {
		super(Separator.class);
	}

	public boolean addEdge(Separator s) {
		return super.addEdge(s.getSource(), s.getTarget(), s);
	}
}
