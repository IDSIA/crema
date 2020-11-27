package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.inference.jtree.BayesianNetworks;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.cliques.FindCliques;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinGraphBuilder;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTreeBuilder;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTreeBuilderKruskal;
import ch.idsia.crema.inference.jtree.algorithm.moralization.Moralize;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.MinDegreeOrdering;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.Triangulate;
import ch.idsia.crema.inference.jtree.algorithm.updating.MessagePassing;
import ch.idsia.crema.inference.jtree.UndirectedGraph;
import gnu.trove.map.hash.TIntIntHashMap;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.Set;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:52
 */
public class AlgorithmTest {

	@Test
	public void moralizeAndTriangulate() {
		// Given: a Bayesian Network BN
//		BayesianNetworks bn = BayesianNetworks.junctionTreeTheoryExample();
		BayesianNetworks bn = BayesianNetworks.junctionTreePropagationTheoryExample();

		// Moralize
		Moralize m = new Moralize();
		m.setModel(bn.network);
		// obtaining a domain graph G for BN (moral graph)
		UndirectedGraph moralGraph = m.exec();

//		assert (moralGraph.getEdge(0, 1) != null);
//		assert (moralGraph.getEdge(4, 5) != null);
//		assert (moralGraph.getEdge(5, 6) != null);
//		assert (moralGraph.getEdge(6, 7) != null);

		// Triangulate
		Triangulate t = new MinDegreeOrdering();
		t.setModel(moralGraph);
		// obtaining a triangulated graph for G
		UndirectedGraph triangulated = t.exec();

//		assert (triangulated.edgeSet().size() == moralGraph.edgeSet().size() + 2);
//		assert (triangulated.vertexSet().size() == moralGraph.vertexSet().size());

		// Find cliques
		FindCliques fc = new FindCliques();
		fc.setModel(triangulated);
		fc.setSequence(t.getSequence());
		Set<Clique> cliques = fc.exec();

//		assert (cliques.size() == 5);
//		assert (cliques.stream().max(Comparator.comparingInt(x -> x.getVariables().length)).get().getVariables().length == 4);
//		assert (cliques.stream().min(Comparator.comparingInt(x -> x.getVariables().length)).get().getVariables().length == 3);

		// Build Join Graph
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setCliques(cliques);
		Graph<Clique, DefaultWeightedEdge> joinGraph = jgb.exec();

		// Find maximal spanning tree
//		JoinTreeBuilder jtb = new JoinTreeBuilderPrim();
		JoinTreeBuilder jtb = new JoinTreeBuilderKruskal();
		jtb.setModel(joinGraph);
		Graph<Clique, DefaultWeightedEdge> joinTree = jtb.exec();

		// now we are ready to perform a belief updating by message passing
		MessagePassing mp = new MessagePassing();
		IntStream.range(0, bn.factors.length).forEach(i -> mp.addFactor(i, bn.factors[i]));
		mp.setModel(joinTree);
		mp.setEvidence(new TIntIntHashMap());
		mp.exec();

		System.out.println();
	}
}