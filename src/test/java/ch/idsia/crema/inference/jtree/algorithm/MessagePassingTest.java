package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.BayesianNetworkContainer;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.cliques.FindCliques;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinGraphBuilder;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTreeBuilder;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTreeBuilderKruskal;
import ch.idsia.crema.inference.jtree.algorithm.moralization.Moralize;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.MinDegreeOrdering;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.Triangulate;
import ch.idsia.crema.inference.jtree.algorithm.updating.MessagePassing;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import gnu.trove.map.hash.TIntIntHashMap;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:52
 */
public class MessagePassingTest {
	BayesianNetwork bn;
	BayesianFactor[] factors;

	@Before
	public void setUp() {
		// given: a Bayesian Network BN
		BayesianNetworkContainer bns = BayesianNetworkContainer.aSimpleBayesianNetwork();
		bn = bns.network;
		factors = bns.factors;
	}

	@Test
	public void testCliquesAlgorithm() {
		// moralization step
		Moralize m = new Moralize();
		m.setInput(bn.getNetwork());
		SparseUndirectedGraph moralGraph = m.exec();

		// triangulation step
		Triangulate t = new MinDegreeOrdering();
		t.setInput(moralGraph);
		SparseUndirectedGraph triangulated = t.exec();

		// Find cliques
		FindCliques fc = new FindCliques();
		fc.setModel(triangulated);
		fc.setSequence(t.getEliminationSequence());
		Set<Clique> cliques = fc.exec();

		// Build Join Graph
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setCliques(cliques);
		Graph<Clique, DefaultWeightedEdge> joinGraph = jgb.exec();

		// Find maximal spanning tree
		JoinTreeBuilder jtb = new JoinTreeBuilderKruskal();
		jtb.setModel(joinGraph);
		Graph<Clique, DefaultWeightedEdge> joinTree = jtb.exec();

		// now we are ready to perform a belief updating by message passing
		MessagePassing mp = new MessagePassing();
		IntStream.range(0, factors.length).forEach(i -> mp.addFactor(i, factors[i]));
		mp.setModel(joinTree);
		mp.setEvidence(new TIntIntHashMap());
		mp.exec();
	}
}