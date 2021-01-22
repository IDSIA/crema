package ch.idsia.crema.inference.bp.join;

import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.cliques.CliqueSet;
import ch.idsia.crema.utility.ArraysUtil;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 18:20
 */
public class JoinTreeBuilderTest {

	CliqueSet cliques;

	@Before
	public void setUp() {
		// source: Jensen, p.123, Fig. 4.15
		int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5, G = 6, H = 7, I = 8, J = 9;

		cliques = new CliqueSet();
		cliques.add(new Clique(new int[]{A, B, C, D}));
		cliques.add(new Clique(new int[]{D, E, F, I}));
		cliques.add(new Clique(new int[]{C, G, H, J}));
		cliques.add(new Clique(new int[]{B, C, D, G}));
		cliques.add(new Clique(new int[]{B, C, D, E}));
	}

	private void checkCliqueCoverage(Graph<Clique, DefaultWeightedEdge> graph) {
		Set<Clique> coveredCliques = new HashSet<>();

		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			Clique source = graph.getEdgeSource(edge);
			Clique target = graph.getEdgeTarget(edge);

			coveredCliques.add(source);
			coveredCliques.add(target);

			int[] intersection = ArraysUtil.intersection(source.getVariables(), target.getVariables());

			assertTrue("No variables shared between " + source + " and " + target, intersection.length > 0);
		}

		assertEquals("Not all cliques are covered", cliques.size(), coveredCliques.size());
	}

	@Test
	public void testJoinTreeWithKruskal() {
		JoinTreeBuilder jtb = new JoinTreeBuilderKruskal();
		jtb.setInput(cliques);
		JoinTree tree = jtb.exec();

		assertEquals(5, tree.vertexSet().size());
		assertEquals(4, tree.edgeSet().size());

		checkCliqueCoverage(tree);
	}

	@Test
	public void testJoinTreeWithKruskal2() {
		// Junction tree propagation - p. 13/25
		final Clique c123 = new Clique(new int[]{1, 2, 3});
		final Clique c247 = new Clique(new int[]{2, 4, 7});
		final Clique c237 = new Clique(new int[]{2, 3, 7});
		final Clique c367 = new Clique(new int[]{3, 6, 7});
		final Clique c5678 = new Clique(new int[]{5, 6, 7, 8});

		cliques = new CliqueSet();
		cliques.add(c123);
		cliques.add(c247);
		cliques.add(c237);
		cliques.add(c367);
		cliques.add(c5678);

		JoinTreeBuilder jtb = new JoinTreeBuilderPrim();
		jtb.setInput(cliques);
		JoinTree tree = jtb.exec();

		assertEquals(5, tree.vertexSet().size());
		assertEquals(4, tree.edgeSet().size());

		assertNotNull(tree.getEdge(c123, c237));
		assertNotNull(tree.getEdge(c237, c247));
		assertNotNull(tree.getEdge(c237, c367));
		assertNotNull(tree.getEdge(c5678, c367));

		assertNull(tree.getEdge(c123, c247));
		assertNull(tree.getEdge(c123, c367));
		assertNull(tree.getEdge(c247, c367));
		assertNull(tree.getEdge(c247, c5678));
		assertNull(tree.getEdge(c237, c5678));
	}

	@Test
	public void testJoinTreeWithPrim() {
		JoinTreeBuilder jtb = new JoinTreeBuilderPrim();
		jtb.setInput(cliques);
		JoinTree tree = jtb.exec();

		assertEquals(5, tree.vertexSet().size());
		assertEquals(4, tree.edgeSet().size());

		checkCliqueCoverage(tree);
	}
}