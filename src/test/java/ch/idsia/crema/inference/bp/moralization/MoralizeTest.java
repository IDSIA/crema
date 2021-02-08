package ch.idsia.crema.inference.bp.moralization;

import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 13:42
 */
public class MoralizeTest {

	@Test
	public void testMoralizationAlgorithm() {
		// source: Jensen, p. 110, Fig. 4.1
		BayesianNetwork bn = new BayesianNetwork();

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

		// moralize
		Moralize m = new Moralize();
		m.setInput(bn.getNetwork());

		// obtaining a domain graph G for BN (moral graph)
		SimpleGraph<Integer, DefaultEdge> moralGraph = m.exec();

		assertEquals("", 7, moralGraph.edgeSet().size());
		assertNotNull("MoralGraph edge A1-A2 is null", moralGraph.getEdge(0, 1));
		assertNotNull("MoralGraph edge A1-A3 is null", moralGraph.getEdge(0, 2));
		assertNotNull("MoralGraph edge A2-A3 is null", moralGraph.getEdge(1, 2));
		assertNotNull("MoralGraph edge A2-A4 is null", moralGraph.getEdge(1, 3));
		assertNotNull("MoralGraph edge A2-A5 is null", moralGraph.getEdge(1, 4));
		assertNotNull("MoralGraph edge A3-A6 is null", moralGraph.getEdge(2, 5));
	}

	@Test
	public void testComplexGraph() {
		// Source: Jensen, p.119, Fig. 4.10 a)
		BayesianNetwork bn = new BayesianNetwork();

		int A1 = bn.addVariable(2);
		int A2 = bn.addVariable(2);
		int A3 = bn.addVariable(2);
		int A4 = bn.addVariable(2);
		int A5 = bn.addVariable(2);
		int A6 = bn.addVariable(2);
		int A7 = bn.addVariable(2);
		int A8 = bn.addVariable(2);

		bn.addParent(A3, A1);
		bn.addParent(A3, A2);
		bn.addParent(A4, A2);
		bn.addParent(A6, A3);
		bn.addParent(A7, A4);
		bn.addParent(A8, A5);
		bn.addParent(A8, A6);
		bn.addParent(A8, A7);

		// moralize
		Moralize m = new Moralize();
		m.setInput(bn.getNetwork());

		// obtaining a domain graph G for BN (moral graph)
		SimpleGraph<Integer, DefaultEdge> moralGraph = m.exec();

		assertEquals(12, moralGraph.edgeSet().size());
		assertNotNull(moralGraph.getEdge(1, 2));
		assertNotNull(moralGraph.getEdge(5, 6));
		assertNotNull(moralGraph.getEdge(5, 7));
		assertNotNull(moralGraph.getEdge(6, 7));
	}
}