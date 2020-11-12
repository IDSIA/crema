package ch.idsia.crema.inference.jtree.algorithm.join;

import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import com.google.common.collect.Sets;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
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
public class JoinGraphBuilderTest {

	Set<Clique> cliques;

	@Before
	public void setUp() {
		// source: Jense, p.123, Fig. 4.15
		int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5, G = 6, H = 7, I = 8, J = 9;

		cliques = Sets.newHashSet(
				new Clique(new int[]{A, B, C, D}),
				new Clique(new int[]{D, E, F, I}),
				new Clique(new int[]{C, G, H, J}),
				new Clique(new int[]{B, C, D, G}),
				new Clique(new int[]{B, C, D, E})
		);

	}

	@Test
	public void testJoinGraphBuilder() {
		JoinGraphBuilder jgb = new JoinGraphBuilder();
		jgb.setCliques(cliques);
		Graph<Clique, DefaultWeightedEdge> joinGraph = jgb.exec();

		assertEquals(5, joinGraph.vertexSet().size());
		assertEquals(9, joinGraph.edgeSet().size());

		for (DefaultWeightedEdge edge : joinGraph.edgeSet()) {
			// check that there is at least one variable shared between cliques
			List<Integer> source = Arrays.stream(joinGraph.getEdgeSource(edge).getVariables()).boxed().collect(Collectors.toList());
			List<Integer> target = Arrays.stream(joinGraph.getEdgeTarget(edge).getVariables()).boxed().collect(Collectors.toList());

			List<Integer> intersection = source.stream().filter(target::contains).collect(Collectors.toList());

			assertTrue(intersection.size() > 0);
		}
	}
}