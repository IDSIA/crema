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

	public void setModel(SparseUndirectedGraph model) {
		this.model = model;
	}

	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}

	public Set<Clique> exec() {

		SparseUndirectedGraph copy = model.copy();

		Set<Clique> cliques = new HashSet<>();

		for (int v : sequence) {
			TIntSet c = new TIntHashSet();
			c.add(v);

			for (DefaultEdge edge : copy.edgesOf(v)) {
				Integer source = copy.getEdgeSource(edge);
				Integer target = copy.getEdgeTarget(edge);

				Integer node = v == target ? source : target;
				c.add(node);
			}

			copy.removeVertex(v);
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
