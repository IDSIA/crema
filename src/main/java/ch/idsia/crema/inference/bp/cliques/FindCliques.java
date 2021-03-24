package ch.idsia.crema.inference.bp.cliques;

import ch.idsia.crema.inference.Algorithm;
import ch.idsia.crema.inference.bp.triangulation.TriangulatedGraph;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.GraphUtil;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.Optional;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    13.02.2018 11:40
 */
public class FindCliques implements Algorithm<TriangulatedGraph, CliqueSet> {

	private TriangulatedGraph model;
	private CliqueSet cliques;

	private int[] sequence;

	/**
	 * @param model a triangulated graph produced by the {@link ch.idsia.crema.inference.bp.triangulation.Triangulate} algorithm.
	 */
	@Override
	public void setInput(TriangulatedGraph model) {
		this.model = model;
		this.sequence = model.getEliminationSequence();
	}

	/**
	 * @param sequence a new elimination sequence to use
	 */
	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the last computed cliques
	 */
	@Override
	public CliqueSet getOutput() {
		return cliques;
	}

	/**
	 * Explore the given triangulated graph searching for all the {@link Clique}s found using the given elimination sequence.
	 *
	 * @return a set of {@link Clique}s found by the algorithm
	 */
	@Override
	public CliqueSet exec() {
		if (sequence == null) throw new IllegalArgumentException("No elimination sequence available.");
		if (model == null) throw new IllegalArgumentException("No model is available");

		final TriangulatedGraph copy = new TriangulatedGraph();
		GraphUtil.copy(model, copy);

		cliques = new CliqueSet();

		// follow the elimination sequence
		for (int v : sequence) {
			final TIntSet c = new TIntHashSet();
			c.add(v);

			// creates a clique composed by the current node and all the nodes in the neighbourhood
			for (DefaultEdge edge : copy.edgesOf(v)) {
				c.add(copy.getEdgeSource(edge));
				c.add(copy.getEdgeTarget(edge));
			}

			copy.removeVertex(v);

			// build the new clique and check if we already have a clique that contains this one
			Clique clique = new Clique(v, ArraysUtil.sort(c.toArray()));

			final Optional<Clique> container = checkIfContains(cliques, clique);
			if (container.isPresent()) {
				// merge with existing clique
				container.get().getV().addAll(clique.getV());
			} else {
				// new clique
				cliques.add(clique);
			}
		}

		return cliques;
	}

	/**
	 * @param cliques set to search in to
	 * @param clique  clique to check
	 * @return true if a clique in the set contains the given clique, otherwise false
	 */
	private Optional<Clique> checkIfContains(Set<Clique> cliques, Clique clique) {
		for (Clique c : cliques) {
			if (c.contains(clique))
				return Optional.of(c);
		}

		return Optional.empty();
	}

}
