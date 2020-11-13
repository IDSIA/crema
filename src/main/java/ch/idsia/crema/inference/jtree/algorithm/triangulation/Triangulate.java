package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import ch.idsia.crema.inference.jtree.algorithm.Algorithm;
import ch.idsia.crema.inference.jtree.algorithm.moralization.MoralGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:34
 * <p>
 * Base class to support multiple triangulation algorithm, such as {@link MinDegreeOrdering}.
 */
public abstract class Triangulate implements Algorithm<MoralGraph, TriangulatedGraph> {

	protected MoralGraph model;
	protected TriangulatedGraph triangulated;

	/**
	 * @param model the moralized graph to apply triangulation
	 */
	@Override
	public void setInput(MoralGraph model) {
		this.model = model;
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

	public abstract TriangulatedGraph exec();
}
