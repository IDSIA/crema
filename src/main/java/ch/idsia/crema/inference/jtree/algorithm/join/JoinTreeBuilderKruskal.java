package ch.idsia.crema.inference.jtree.algorithm.join;

import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 09:32
 */
public class JoinTreeBuilderKruskal extends JoinTreeBuilder {

	/**
	 * Uses the {@link PrimMinimumSpanningTree} algorithm to compute the maximal spanning tree of the given model.
	 *
	 * @return the maximal spanning tree found
	 */
	@Override
	protected SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> getMaximalSpanningTree(JoinTree model) {
		KruskalMinimumSpanningTree<Clique, DefaultWeightedEdge> pst = new KruskalMinimumSpanningTree<>(model);

		return pst.getSpanningTree();
	}
}
