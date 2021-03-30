package ch.idsia.crema.inference.bp.moralization;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.inference.Algorithm;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:11
 */
public class Moralize<F extends GenericFactor> implements Algorithm<DAGModel<F>, SimpleGraph<Integer, DefaultEdge>> {

	private DAGModel<F> model;
	private SimpleGraph<Integer, DefaultEdge> moralized;

	/**
	 * @param model the model to moralize
	 */
	@Override
	public void setInput(DAGModel<F> model) {
		this.model = model;
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
		if (model == null) throw new IllegalArgumentException("No model available");

		// copy model, this will keep the existing edges
		moralized = new SimpleGraph<>(DefaultEdge.class);

		// add all the vertices to the new graph
		for (int v : model.getVariables()) {
			moralized.addVertex(v);
		}

		// build up from leaves
		TIntSet nodes = new TIntHashSet(model.getLeaves());

		// apply moralization
		do {
			final TIntSet slack = new TIntHashSet();

			for (int v : nodes.toArray()) {
				final int[] parents = model.getParents(v);
				slack.addAll(parents);

				// add existing edges
				for (int p : parents)
					moralized.addEdge(p, v);

				// marry the parents and add edges to couples
				for (int p1 : parents)
					for (int p2 : parents)
						if (p1 != p2)
							moralized.addEdge(p1, p2);
			}
			nodes = slack;
		} while (!nodes.isEmpty());

		return moralized;
	}
}
