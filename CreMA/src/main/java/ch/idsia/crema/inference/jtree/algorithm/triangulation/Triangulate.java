package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:34
 */
public abstract class Triangulate {

	protected SparseUndirectedGraph model;
	protected SparseUndirectedGraph triangulated;

	// elimination sequence
	protected List<Integer> sequence;

	public void setModel(SparseUndirectedGraph model) {
		this.model = model;
	}

	public int[] getSequence() {
		return sequence.stream().mapToInt(x -> x).toArray();
	}

	public Graph<Integer, DefaultEdge> getTriangulated() {
		return triangulated;
	}

	public abstract SparseUndirectedGraph exec();
}
