package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.BayesianNetworkContainer;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.cliques.FindCliques;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTreeBuilderKruskal;
import ch.idsia.crema.inference.jtree.algorithm.junction.JunctionTreeBuilder;
import ch.idsia.crema.inference.jtree.algorithm.junction.Separator;
import ch.idsia.crema.inference.jtree.algorithm.moralization.Moralize;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.MinDegreeOrdering;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import org.jgrapht.Graph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
	@SuppressWarnings("rawtypes")
	public void testMessagePassingAlgorithm() {
		List<Algorithm> stages = Arrays.asList(
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

		Pipe<SparseDirectedAcyclicGraph, Graph<Clique, Separator>> pipeline = new Pipe<>(stages);
		pipeline.setInput(bn.getNetwork());
		Graph<Clique, Separator> junctionTree = pipeline.exec();

		// now we are ready to perform a belief updating by message passing
		// TODO
//		MessagePassing mp = new MessagePassing();
//		IntStream.range(0, factors.length).forEach(i -> mp.addFactor(i, factors[i]));
//		mp.setModel(joinTree);
//		mp.setEvidence(new TIntIntHashMap());
//		mp.exec();
	}
}