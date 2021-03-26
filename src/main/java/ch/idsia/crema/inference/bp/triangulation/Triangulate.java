package ch.idsia.crema.inference.bp.triangulation;

import ch.idsia.crema.inference.Algorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:34
 * <p>
 * Base class to support multiple triangulation algorithm, such as {@link MinDegreeOrdering}.
 */
public abstract class Triangulate implements Algorithm<SimpleGraph<Integer, DefaultEdge>, TriangulatedGraph> {

	protected SimpleGraph<Integer, DefaultEdge> network;
	protected TriangulatedGraph triangulated;

	protected Boolean hasPerfectEliminationSequence = true;

	/**
	 * @param model the moralized graph to apply triangulation
	 */
	@Override
	public void setInput(SimpleGraph<Integer, DefaultEdge> model) {
		this.network = model;
	}

	public TriangulatedGraph getOutput() {
		return triangulated;
	}

	/**
	 * @return the last found elimination sequence
	 */
	public int[] getEliminationSequence() {
		return triangulated.getEliminationSequence();
	}

	/**
	 * @return the triangulated model
	 */
	public TriangulatedGraph getTriangulated() {
		return triangulated;
	}

	public Boolean getHasPerfectEliminationSequence() {
		return hasPerfectEliminationSequence;
	}

	public abstract TriangulatedGraph exec();
}
