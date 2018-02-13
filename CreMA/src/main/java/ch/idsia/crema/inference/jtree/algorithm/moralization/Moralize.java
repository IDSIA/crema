package ch.idsia.crema.inference.jtree.algorithm.moralization;

import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:11
 */
public class Moralize {

	private SparseDirectedAcyclicGraph model;

	public void setModel(SparseDirectedAcyclicGraph model) {
		this.model = model;
	}

	public SparseUndirectedGraph exec() {

		SparseUndirectedGraph moralized = new SparseUndirectedGraph();

		// add all the vertices to the new graph
		model.vertexSet().forEach(moralized::addVertex);

		// apply moralization
		model.vertexSet().forEach(v -> {
			for (int parent : model.getParents(v)) {
				// keep existing edges
				moralized.addEdge(parent, v);

				// marry the parents and add edges to couples
				for (int other : model.getParents(v)) {
					if (other == parent) continue;
					moralized.addEdge(parent, other);
				}
			}
		});

		return moralized;
	}
}
