package ch.idsia.crema.inference.jtree.algorithm.moralization;

import ch.idsia.crema.inference.jtree.algorithm.Algorithm;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:11
 */
public class Moralize implements Algorithm<SparseDirectedAcyclicGraph, SparseUndirectedGraph> {

	private SparseDirectedAcyclicGraph model;
	private SparseUndirectedGraph moralized;

	/**
	 * @param model the model to moralize
	 */
	@Override
	public void setInput(SparseDirectedAcyclicGraph model) {
		this.model = model;
	}

	/**
	 * @return the last moralized graph found
	 */
	@Override
	public SparseUndirectedGraph getOutput() {
		return moralized;
	}

	/**
	 * Convert a {@link SparseDirectedAcyclicGraph} into a {@link SparseUndirectedGraph} using the moralization
	 * algorithm over the given model.
	 *
	 * @return a moralized {@link SparseUndirectedGraph}
	 */
	@Override
	public SparseUndirectedGraph exec() {
		if (model == null) throw new IllegalArgumentException("No model available");

		moralized = new SparseUndirectedGraph();

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
