package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.inference.jtree.BayesianNetworks;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.cliques.FindCliques;
import ch.idsia.crema.inference.jtree.algorithm.moralization.Moralize;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.MinDegreeOrdering;
import ch.idsia.crema.inference.jtree.algorithm.triangulation.Triangulate;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import org.junit.Test;

import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:52
 */
public class AlgorithmTest {

	@Test
	public void moralizeAndTriangulate() {
		BayesianNetworks bn = BayesianNetworks.junctionTreeTheoryExample();

		Moralize m = new Moralize();
		m.setModel(bn.network.getNetwork());
		SparseUndirectedGraph moralized = m.exec();

		assert (moralized.getEdge(0, 1) != null);
		assert (moralized.getEdge(4, 5) != null);
		assert (moralized.getEdge(5, 6) != null);
		assert (moralized.getEdge(6, 7) != null);

		Triangulate t = new MinDegreeOrdering();
		t.setModel(moralized);
		SparseUndirectedGraph triangulated = t.exec();

		assert (triangulated.edgeSet().size() == moralized.edgeSet().size() + 2);
		assert (triangulated.vertexSet().size() == moralized.vertexSet().size());

		FindCliques c = new FindCliques();
		c.setModel(triangulated);
		c.setSequence(t.getSequence());
		Set<Clique> cliques = c.exec();
	}
}