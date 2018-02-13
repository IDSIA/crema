package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.inference.jtree.BayesianNetworks;
import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:52
 */
public class MoralizeTest {

	@Test
	public void run() {
		BayesianNetworks bn = BayesianNetworks.junctionTreeTheoryExample();

		Moralize m = new Moralize();
		m.setModel(bn.network.getNetwork());
		SparseUndirectedGraph moralized = m.run();

		assert (moralized.getEdge(0, 1) != null);
		assert (moralized.getEdge(4, 5) != null);
		assert (moralized.getEdge(5, 6) != null);
		assert (moralized.getEdge(6, 7) != null);

		Triangulate t = new Triangulate();
		t.setModel(moralized);
		SparseUndirectedGraph triangulated = t.run();

		assert (triangulated.edgeSet().size() == moralized.edgeSet().size() + 2);
		assert (triangulated.vertexSet().size() == moralized.vertexSet().size());
	}
}