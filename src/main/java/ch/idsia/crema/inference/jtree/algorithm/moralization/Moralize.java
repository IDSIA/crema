package ch.idsia.crema.inference.jtree.algorithm.moralization;

import ch.idsia.crema.inference.jtree.UndirectedGraph;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:11
 */
public class Moralize {

	private BayesianNetwork model;
	private UndirectedGraph moralized;

	/**
	 * @param model the model to moralize
	 */
	public void setModel(BayesianNetwork model) {
		this.model = model;
	}

	/**
	 * @return the last moralized graph found
	 */
	public UndirectedGraph getMoralized() {
		return moralized;
	}

	/**
	 * Convert a {@link DAGModel} into a {@link UndirectedGraph} using the moralization
	 * algorithm over the given model.
	 *
	 * @return a moralized {@link UndirectedGraph}
	 */
	public UndirectedGraph exec() {
		if (model == null) throw new IllegalArgumentException("No model available");

		moralized = new UndirectedGraph();

		// add all the vertices to the new graph
		model.getNetwork().vertexSet().forEach(moralized::addVertex);

		// apply moralization
		model.getNetwork().vertexSet().forEach(v -> {
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
