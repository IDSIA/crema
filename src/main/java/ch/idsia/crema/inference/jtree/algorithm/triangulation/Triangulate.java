package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import ch.idsia.crema.inference.jtree.UndirectedGraph;
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
public abstract class Triangulate {

	protected UndirectedGraph model;
	protected UndirectedGraph triangulated;

	// elimination sequence
	protected List<Integer> sequence;

	/**
	 * @param model the moralized graph to apply triangulation
	 */
	public void setModel(UndirectedGraph model) {
		this.model = model;
	}

	/**
	 * @return the last found elimination sequence
	 */
	public int[] getSequence() {
		return sequence.stream().mapToInt(x -> x).toArray();
	}

	/**
	 * @return the last computed triangulated graph
	 */
	public Graph<Integer, DefaultEdge> getTriangulated() {
		return triangulated;
	}

	public abstract UndirectedGraph exec();
}
