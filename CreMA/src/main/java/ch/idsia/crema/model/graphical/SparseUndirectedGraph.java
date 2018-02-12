package ch.idsia.crema.model.graphical;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 13:32
 */
public class SparseUndirectedGraph extends SimpleGraph<Integer, DefaultEdge> {

	public SparseUndirectedGraph() {
		super(DefaultEdge.class);
	}

}
