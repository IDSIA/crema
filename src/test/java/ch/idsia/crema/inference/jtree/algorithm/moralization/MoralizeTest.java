package ch.idsia.crema.inference.jtree.algorithm.moralization;

import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 13:42
 */
public class MoralizeTest {
	BayesianNetwork bn;

	@Before
	public void setUp() {
		// source: Jensen, p. 110, Fig. 4.1
		bn = new BayesianNetwork();

		int A1 = bn.addVariable(2);
		int A2 = bn.addVariable(2);
		int A3 = bn.addVariable(2);
		int A4 = bn.addVariable(2);
		int A5 = bn.addVariable(2);
		int A6 = bn.addVariable(2);

		bn.addParent(A2, A1);
		bn.addParent(A3, A1);
		bn.addParent(A4, A2);
		bn.addParent(A5, A2);
		bn.addParent(A5, A3);
		bn.addParent(A6, A3);
	}

	@Test
	public void testMoralizationAlgorithm() {
		// moralize
		Moralize m = new Moralize();
		m.setInput(bn.getNetwork());

		// obtaining a domain graph G for BN (moral graph)
		SparseUndirectedGraph moralGraph = m.exec();

		assertEquals("", moralGraph.edgeSet().size(), 7);
		assertNotNull("MoralGraph edge A1-A2 is null", moralGraph.getEdge(0, 1));
		assertNotNull("MoralGraph edge A1-A3 is null", moralGraph.getEdge(0, 2));
		assertNotNull("MoralGraph edge A2-A3 is null", moralGraph.getEdge(1, 2));
		assertNotNull("MoralGraph edge A2-A4 is null", moralGraph.getEdge(1, 3));
		assertNotNull("MoralGraph edge A2-A5 is null", moralGraph.getEdge(1, 4));
		assertNotNull("MoralGraph edge A3-A6 is null", moralGraph.getEdge(2, 5));
	}
}