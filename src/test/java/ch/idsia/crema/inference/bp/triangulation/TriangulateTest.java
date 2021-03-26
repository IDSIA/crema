package ch.idsia.crema.inference.bp.triangulation;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 13:50
 */
public class TriangulateTest {

	@Test
	public void testInvertedVStructure() {
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

		graph.addVertex(0);
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(0, 1);
		graph.addEdge(0, 2);

		Triangulate t = new MinDegreeOrdering();
		t.setInput(graph);

		TriangulatedGraph triangulated = t.exec();

		assertEquals(3, triangulated.vertexSet().size());
		assertEquals(2, triangulated.edgeSet().size());

		int[] eliminationSequence = t.getEliminationSequence();
		assertEquals(graph.vertexSet().size(), eliminationSequence.length, "Elimination sequence doesn't cover all vertex");
	}

	@Test
	public void testTriangulationAlgorithmMinDegreeOrdering() {
		// Source: Jensen, p.119, Fig. 4.10 a)
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

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

		// triangulate
		Triangulate t = new MinDegreeOrdering();
		t.setInput(graph);

		// obtaining a triangulated graph for G
		TriangulatedGraph triangulated = t.exec();

		assertEquals(graph.vertexSet().size(), triangulated.vertexSet().size(), "TriangulatedGraph has a different number of vertex");
		assertEquals(graph.edgeSet().size() + 1, triangulated.edgeSet().size(), "TriangulatedGraph has missing edges");

		int[] eliminationSequence = t.getEliminationSequence();
		assertEquals(graph.vertexSet().size(), eliminationSequence.length, "Elimination sequence doesn't cover all vertex");
	}

	@Test
	public void testAlreadyTriangulated() {
		// junction tree propagation p. 14/25
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

		graph.addVertex(1);
		graph.addVertex(2);
		graph.addVertex(3);
		graph.addVertex(4);
		graph.addVertex(5);
		graph.addVertex(6);

		graph.addEdge(1, 2);
		graph.addEdge(1, 3);
		graph.addEdge(2, 3);
		graph.addEdge(2, 4);
		graph.addEdge(2, 5);
		graph.addEdge(3, 5);
		graph.addEdge(3, 6);

		Triangulate t = new MinDegreeOrdering();
		t.setInput(graph);

		TriangulatedGraph triangulated = t.exec();

		assertEquals(6, triangulated.vertexSet().size());
		assertEquals(7, triangulated.edgeSet().size());

		int[] eliminationSequence = t.getEliminationSequence();
		assertEquals(graph.vertexSet().size(), eliminationSequence.length, "Elimination sequence doesn't cover all vertex");
	}

	@Test
	public void testNotTriangulated() {
		// Source: Jensen, p.119, Fig. 4.10 a)
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

		graph.addVertex(1);
		graph.addVertex(2);
		graph.addVertex(3);
		graph.addVertex(4);
		graph.addVertex(5);
		graph.addVertex(6);
		graph.addVertex(7);
		graph.addVertex(8);

		graph.addEdge(1, 2);
		graph.addEdge(1, 3);
		graph.addEdge(2, 3);
		graph.addEdge(2, 4);
		graph.addEdge(3, 6);
		graph.addEdge(4, 7);
		graph.addEdge(5, 6);
		graph.addEdge(5, 7);
		graph.addEdge(5, 8);
		graph.addEdge(6, 7);
		graph.addEdge(6, 8);
		graph.addEdge(7, 8);

		// triangulate
		Triangulate t = new MinDegreeOrdering();
		t.setInput(graph);

		// obtaining a triangulated graph for G
		TriangulatedGraph triangulated = t.exec();

		assertEquals(graph.vertexSet().size(), triangulated.vertexSet().size(), "TriangulatedGraph has a different number of vertex");
		assertEquals(14, triangulated.edgeSet().size(), "TriangulatedGraph has missing edges");

		int newEdges = 0;
		if (triangulated.getEdge(2, 7) != null) newEdges++;
		if (triangulated.getEdge(2, 6) != null) newEdges++;
		if (triangulated.getEdge(3, 4) != null) newEdges++;
		if (triangulated.getEdge(3, 7) != null) newEdges++;
		if (triangulated.getEdge(4, 6) != null) newEdges++;

		assertEquals(2, newEdges, "Not enough new edges added");

		int[] eliminationSequence = t.getEliminationSequence();
		assertEquals(graph.vertexSet().size(), eliminationSequence.length, "Elimination sequence doesn't cover all vertex");
	}
}