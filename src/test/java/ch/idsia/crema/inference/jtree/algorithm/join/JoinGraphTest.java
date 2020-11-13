package ch.idsia.crema.inference.jtree.algorithm.join;

import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import com.google.common.collect.Sets;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    12.11.2020 18:20
 */
public class JoinGraphTest {

	Set<Clique> cliques;

	@Before
	public void setUp() {
		// source: Jensen, p.123, Fig. 4.15
		int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5, G = 6, H = 7, I = 8, J = 9;

		cliques = Sets.newHashSet(
				new Clique(new int[]{A, B, C, D}),
				new Clique(new int[]{D, E, F, I}),
				new Clique(new int[]{C, G, H, J}),
				new Clique(new int[]{B, C, D, G}),
				new Clique(new int[]{B, C, D, E})
		);
	}

	private void checkCliqueCoverage(Graph<Clique, DefaultWeightedEdge> graph) {
		Set<Clique> coveredCliques = new HashSet<>();

		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			Clique sClique = graph.getEdgeSource(edge);
			Clique tClique = graph.getEdgeTarget(edge);

			coveredCliques.add(sClique);
			coveredCliques.add(tClique);

			List<Integer> source = Arrays.stream(sClique.getVariables()).boxed().collect(Collectors.toList());
			List<Integer> target = Arrays.stream(tClique.getVariables()).boxed().collect(Collectors.toList());

			List<Integer> intersection = source.stream().filter(target::contains).collect(Collectors.toList());

			assertTrue("No variables shared between " + sClique + " and " + tClique, intersection.size() > 0);
		}

		assertEquals("Not all cliques are covered", cliques.size(), coveredCliques.size());
	}

	@Test
	public void testJoinGraphBuilder() {
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setInput(cliques);
		Graph<Clique, DefaultWeightedEdge> graph = jgb.exec();

		assertEquals(5, graph.vertexSet().size());
		assertEquals(9, graph.edgeSet().size());

		checkCliqueCoverage(graph);
	}

	@Test
	public void testJoinTreeWithKruskal() {
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setInput(cliques);
		Graph<Clique, DefaultWeightedEdge> graph = jgb.exec();

		JoinTreeBuilder jtb = new JoinTreeBuilderKruskal();
		jtb.setInput(graph);
		Graph<Clique, DefaultWeightedEdge> tree = jtb.exec();

		assertEquals(5, tree.vertexSet().size());
		assertEquals(4, tree.edgeSet().size());

		checkCliqueCoverage(tree);
	}

	@Test
	public void testJoinTreeWithPrim() {
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setInput(cliques);
		Graph<Clique, DefaultWeightedEdge> graph = jgb.exec();

		JoinTreeBuilder jtb = new JoinTreeBuilderPrim();
		jtb.setInput(graph);
		Graph<Clique, DefaultWeightedEdge> tree = jtb.exec();

		assertEquals(5, tree.vertexSet().size());
		assertEquals(4, tree.edgeSet().size());

		checkCliqueCoverage(tree);
	}
}