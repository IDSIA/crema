package ch.idsia.crema.inference.jtree.algorithm.updating;

import ch.idsia.crema.inference.jtree.algorithm.Pipe;
import ch.idsia.crema.inference.jtree.algorithm.cliques.FindCliques;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTreeBuilderKruskal;
import ch.idsia.crema.inference.jtree.algorithm.junction.JunctionTree;
import ch.idsia.crema.inference.jtree.algorithm.junction.JunctionTreeBuilder;
import ch.idsia.crema.inference.jtree.algorithm.moralization.Moralize;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.MinDegreeOrdering;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.11.2020 18:22
 */
public class GraphToJunctionTreePipe extends Pipe<SparseDirectedAcyclicGraph, JunctionTree> {

	public GraphToJunctionTreePipe() {
		this.stages = Arrays.asList(
				// moralization step
				new Moralize(),
				// triangulation step
				new MinDegreeOrdering(),
				// find cliques
				new FindCliques(),
				// Find maximal spanning tree
				new JoinTreeBuilderKruskal(),
				// JunctionTree
				new JunctionTreeBuilder()
		);
	}
}
