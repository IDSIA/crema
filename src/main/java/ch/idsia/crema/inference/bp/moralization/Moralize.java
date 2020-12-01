package ch.idsia.crema.inference.bp.moralization;

import ch.idsia.crema.inference.Algorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:11
 */
public class Moralize implements Algorithm<DirectedAcyclicGraph<Integer, DefaultEdge>, SimpleGraph<Integer, DefaultEdge>> {

	private DirectedAcyclicGraph<Integer, DefaultEdge> network;
	private SimpleGraph<Integer, DefaultEdge> moralized;

	/**
	 * @param network the model to moralize
	 */
	@Override
	public void setInput(DirectedAcyclicGraph<Integer, DefaultEdge> network) {
		this.network = network;
	}

	/**
	 * @return the last moralized graph found
	 */
	@Override
	public SimpleGraph<Integer, DefaultEdge> getOutput() {
		return moralized;
	}

	/**
	 * Convert a {@link DirectedAcyclicGraph} into a {@link SimpleGraph} using the moralization
	 * algorithm over the given model.
	 *
	 * @return a moralized {@link SimpleGraph}
	 */
	@Override
	public SimpleGraph<Integer, DefaultEdge> exec() {
		if (network == null) throw new IllegalArgumentException("No model available");

		// copy model, this will keep the existing edges
		moralized = new SimpleGraph<>(DefaultEdge.class);

		// add all the vertices to the new graph
		network.vertexSet().forEach(moralized::addVertex);

		// apply moralization
		network.vertexSet().forEach(v -> {
			for (DefaultEdge e1 : network.incomingEdgesOf(v)) {
				int parent = network.getEdgeSource(e1);

				// keep existing edges
				moralized.addEdge(parent, v);

				// marry the parents and add edges to couples
				for (DefaultEdge e2 : network.incomingEdgesOf(v)) {
					int other = network.getEdgeSource(e2);
					if (other == parent) continue;
					moralized.addEdge(parent, other);
				}
			}
		});

		return moralized;
	}
}
