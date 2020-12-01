package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.Pipe;
import ch.idsia.crema.inference.bp.cliques.FindCliques;
import ch.idsia.crema.inference.bp.join.JoinTreeBuilderKruskal;
import ch.idsia.crema.inference.bp.junction.JunctionTree;
import ch.idsia.crema.inference.bp.junction.JunctionTreeBuilder;
import ch.idsia.crema.inference.bp.moralization.Moralize;
import ch.idsia.crema.inference.bp.triangulation.MinDegreeOrdering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.11.2020 18:22
 */
public class GraphToJunctionTreePipe<F extends Factor<F>> extends Pipe<DirectedAcyclicGraph<Integer, DefaultEdge>, JunctionTree<F>> {

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
				new JunctionTreeBuilder<F>()
		);
	}
}
