package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.BayesianNetworkContainer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.11.2020 18:25
 */
public class GraphToJunctionTreePipeTest {

	@Test
	@Disabled
	public void testPipelineToDot() {
		// given: a Bayesian Network BN
		BayesianNetworkContainer bns = BayesianNetworkContainer.aSimpleBayesianNetwork();
		DirectedAcyclicGraph<Integer, DefaultEdge> bn = bns.network.getNetwork();

		GraphToJunctionTreePipe<BayesianFactor> pipe = new GraphToJunctionTreePipe<>();
		pipe.setInput(bn);
		pipe.exec();

		/*
		TODO: find a way to draw graphs using dot

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("0_BayesianNetwork.dot"))) {
			String dot = new DotSerialize().run(bns.network);
			bw.write(dot);
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("1_moralization.dot"))) {
			Moralize moralize = (Moralize) pipe.getStages().get(0);
			MoralGraph moralized = moralize.getOutput();

			String dot = DotSerialize.serialize(moralized);
			bw.write(dot);
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("2_triangulation.dot"))) {
			MinDegreeOrdering mdo = (MinDegreeOrdering) pipe.getStages().get(1);
			TriangulatedGraph triangulated = mdo.getOutput();

			String dot = DotSerialize.serialize(triangulated);
			bw.write(dot);
		}

//		try (BufferedWriter bw = new BufferedWriter(new FileWriter("3_cliqueset.dot"))) {
//			FindCliques fc = (FindCliques) pipe.getStages().get(2);
//			CliqueSet output = fc.getOutput();
//
//			String dot = new DotSerialize().run(output);
//			bw.write(dot);
//		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("4_jointree.dot"))) {
			JoinTreeBuilderKruskal jtbk = (JoinTreeBuilderKruskal) pipe.getStages().get(3);
			JoinTree jointree = jtbk.getOutput();

			String dot = DotSerialize.serialize(jointree);
			bw.write(dot);
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("5_junctiontree.dot"))) {
			JunctionTreeBuilder jtb = (JunctionTreeBuilder) pipe.getStages().get(4);
			JunctionTree junctionTree = jtb.getOutput();

			String dot = DotSerialize.serialize(junctionTree);
			bw.write(dot);
		}
		*/
	}
}