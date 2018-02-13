package ch.idsia.crema.inference.jtree.algorithm.cliques;

import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    13.02.2018 11:40
 */
public class FindCliques {

	private SparseUndirectedGraph model;

	private int[] sequence;

	private Set<Clique> cliques;

	public void setModel(SparseUndirectedGraph model) {
		this.model = model;
	}

	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}

	public Set<Clique> getCliques() {
		return cliques;
	}

	public Set<Clique> exec() {

		SparseUndirectedGraph copy = model.copy();
		cliques = new HashSet<>();

		// follow the elimination sequence
		for (int v : sequence) {
			TIntSet c = new TIntHashSet();
			c.add(v);

			// creates a clique composed by the current node and all the nodes in the neighbourhood
			for (DefaultEdge edge : copy.edgesOf(v)) {
				Integer source = copy.getEdgeSource(edge);
				Integer target = copy.getEdgeTarget(edge);

				Integer node = v == target ? source : target;
				c.add(node);
			}

			copy.removeVertex(v);

			// build the new clique and check if we already have a clique that contains this one
			Clique clique = new Clique(ArraysUtil.sort(c.toArray()));

			if (!checkIfContains(cliques, clique)) {
				cliques.add(clique);
			}
		}

		return cliques;
	}

	private boolean checkIfContains(Set<Clique> cliques, Clique clique) {
		for (Clique c : cliques) {
			if (c.contains(clique))
				return true;
		}

		return false;
	}

}
