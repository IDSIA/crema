package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import ch.idsia.crema.model.graphical.SparseUndirectedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 14:49
 * <p>
 * This is a support class for {@link Triangulate} multiple output.
 */
public class TriangulatedGraph extends SparseUndirectedGraph {

	private int[] eliminationSequence;

	public int[] getEliminationSequence() {
		return eliminationSequence;
	}

	public void setEliminationSequence(int[] eliminationSequence) {
		this.eliminationSequence = eliminationSequence;
	}
}
