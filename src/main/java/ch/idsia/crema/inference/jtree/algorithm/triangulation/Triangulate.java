package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import ch.idsia.crema.inference.jtree.algorithm.Algorithm;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:34
 * <p>
 * Base class to support multiple triangulation algorithm, such as {@link MinDegreeOrdering}.
 */
public abstract class Triangulate implements Algorithm<SparseUndirectedGraph, SparseUndirectedGraph> {

	protected SparseUndirectedGraph model;
	protected SparseUndirectedGraph triangulated;
	protected List<Integer> eliminationSequence;

	/**
	 * @param model the moralized graph to apply triangulation
	 */
	@Override
	public void setInput(SparseUndirectedGraph model) {
		this.model = model;
	}

	public SparseUndirectedGraph getOutput() {
		return triangulated;
	}

	/**
	 * @return the last found elimination sequence
	 */
	public int[] getEliminationSequence() {
		return eliminationSequence.stream().mapToInt(x -> x).toArray();
	}

	/**
	 * @return the last computed triangulated graph
	 */
	public Graph<Integer, DefaultEdge> getTriangulated() {
		return triangulated;
	}

	public abstract SparseUndirectedGraph exec();
}
