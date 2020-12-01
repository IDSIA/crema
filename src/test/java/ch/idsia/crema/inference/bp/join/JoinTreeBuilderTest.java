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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
	public void testJoinTreeWithPrim() {
		JoinTreeBuilder jtb = new JoinTreeBuilderPrim();
		jtb.setInput(cliques);
		JoinTree tree = jtb.exec();

		assertEquals(5, tree.vertexSet().size());
		assertEquals(4, tree.edgeSet().size());

		checkCliqueCoverage(tree);
	}
}