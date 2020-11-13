package ch.idsia.crema.inference.jtree.algorithm.cliques;

import ch.idsia.crema.inference.jtree.algorithm.triangulation.TriangulatedGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 14:13
 */
public class FindCliquesTest {
	TriangulatedGraph graph;
	int[] eliminationSequence;

	@Before
	public void setUp() {
		// source: Jensen, p. 123, Fig 4.14
		graph = new TriangulatedGraph();
		int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5, G = 6, H = 7, I = 8, J = 9;
		int[] vertices = {A, B, C, D, E, F, G, H, I, J};
		int[][] edges = {
				{A, B}, {A, C}, {A, D},
				{B, C}, {B, D}, {B, E}, {B, G},
				{C, D}, {C, E}, {C, G}, {C, H}, {C, J},
				{D, E}, {D, F}, {D, I}, {D, G},
				{E, F}, {E, I},
				{F, I},
				{G, H}, {G, J},
				{H, J}
		};

		Arrays.stream(vertices).forEach(i -> graph.addVertex(i));
		Arrays.stream(edges).forEach(e -> graph.addEdge(e[0], e[1]));

		eliminationSequence = new int[]{A, F, I, H, J, G, B, C, D, E};
	}

	@Test
	public void testFindingCliquesAlgorithm() {
		// Find cliques
		FindCliques fc = new FindCliques();
		fc.setInput(graph);
		fc.setSequence(eliminationSequence);
		Set<Clique> cliques = fc.exec();

		assertEquals(5, cliques.size());
		assertEquals(4, cliques.stream().max(
				Comparator.comparingInt(x -> x.getVariables().length)).orElseGet(Clique::new).getVariables().length
		);
		assertEquals(4, cliques.stream().min(
				Comparator.comparingInt(x -> x.getVariables().length)).orElseGet(Clique::new).getVariables().length
		);
	}
}