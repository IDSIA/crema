package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.inference.jtree.BayesianNetworks;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.cliques.FindCliques;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinGraphBuilder;
import ch.idsia.crema.inference.jtree.algorithm.moralization.Moralize;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.MinDegreeOrdering;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.Triangulate;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.Comparator;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:52
 */
public class AlgorithmTest {

	@Test
	public void moralizeAndTriangulate() {
		// Given: a Bayesian Network BN
		BayesianNetworks bn = BayesianNetworks.junctionTreeTheoryExample();

		// Moralize
		Moralize m = new Moralize();
		m.setModel(bn.network.getNetwork());
		// obtaining a domain graph G for BN (moral graph)
		SparseUndirectedGraph moralGraph = m.exec();

		assert (moralGraph.getEdge(0, 1) != null);
		assert (moralGraph.getEdge(4, 5) != null);
		assert (moralGraph.getEdge(5, 6) != null);
		assert (moralGraph.getEdge(6, 7) != null);

		// Triangulate
		Triangulate t = new MinDegreeOrdering();
		t.setModel(moralGraph);
		// obtaining a triangulated graph for G
		SparseUndirectedGraph triangulated = t.exec();

		assert (triangulated.edgeSet().size() == moralGraph.edgeSet().size() + 2);
		assert (triangulated.vertexSet().size() == moralGraph.vertexSet().size());

		// Find cliques
		FindCliques fc = new FindCliques();
		fc.setModel(triangulated);
		fc.setSequence(t.getSequence());
		Set<Clique> cliques = fc.exec();

		assert (cliques.size() == 5);
		assert (cliques.stream().max(Comparator.comparingInt(x -> x.getVariables().length)).get().getVariables().length == 4);
		assert (cliques.stream().min(Comparator.comparingInt(x -> x.getVariables().length)).get().getVariables().length == 3);

		// Build Join Graph
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setCliques(cliques);
		Graph<Clique, DefaultWeightedEdge> joinGraph = jgb.exec();

		// Find maximal spanning tree
		PrimMinimumSpanningTree<Clique, DefaultWeightedEdge> pst = new PrimMinimumSpanningTree<>(joinGraph);
		SpanningTreeAlgorithm.SpanningTree<DefaultWeightedEdge> joinTree = pst.getSpanningTree();

		for (DefaultWeightedEdge edge : joinTree) {
			System.out.println(edge);
		}

		// now we are ready to perform a belief updating by message passing

		System.out.println();
	}
}