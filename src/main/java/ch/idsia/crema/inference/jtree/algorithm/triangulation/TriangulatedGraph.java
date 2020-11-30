package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 14:49
 * <p>
 * This is a support class for {@link Triangulate} multiple output.
 */
public class TriangulatedGraph extends SimpleGraph<Integer, DefaultEdge> {

	private int[] eliminationSequence;

	public TriangulatedGraph() {
		super(DefaultEdge.class);
	}

	public int[] getEliminationSequence() {
		return eliminationSequence;
	}

	public void setEliminationSequence(int[] eliminationSequence) {
		this.eliminationSequence = eliminationSequence;
	}
}
