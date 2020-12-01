package ch.idsia.crema.inference.bp.triangulation;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 13:50
 */
public class TriangulateTest {
	SimpleGraph<Integer, DefaultEdge> graph;

	@Before
	public void setUp() {
		// Source: Jensen, p.119, Fig. 4.10 a)
		graph = new SimpleGraph<>(DefaultEdge.class);

		graph.addVertex(0);
		graph.addVertex(1);
		graph.addVertex(2);
		graph.addVertex(3);
		graph.addVertex(4);

		graph.addEdge(0, 1);
		graph.addEdge(0, 2);
		graph.addEdge(0, 3);
		graph.addEdge(4, 1);
		graph.addEdge(4, 2);
		graph.addEdge(4, 3);
	}

	@Test
	public void testTriangulationAlgorithmMinDegreeOrdering() {
		// triangulate
		Triangulate t = new MinDegreeOrdering();
		t.setInput(graph);

		// obtaining a triangulated graph for G
		TriangulatedGraph triangulated = t.exec();

		assertEquals("TriangulatedGraph has a different number of vertex", graph.vertexSet().size(), triangulated.vertexSet().size());
		assertEquals("TriangulatedGraph has missing edges", graph.edgeSet().size() + 1, triangulated.edgeSet().size());

		int[] eliminationSequence = t.getEliminationSequence();
		assertEquals("Elimination sequence dosn't cover all vertex", graph.vertexSet().size(), eliminationSequence.length);
	}
}