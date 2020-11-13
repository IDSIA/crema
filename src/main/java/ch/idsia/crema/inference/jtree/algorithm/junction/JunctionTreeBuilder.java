package ch.idsia.crema.inference.jtree.algorithm.junction;

import ch.idsia.crema.inference.jtree.algorithm.Algorithm;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.join.JoinTree;
import ch.idsia.crema.utility.ArraysUtil;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 15:19
 */
public class JunctionTreeBuilder implements Algorithm<JoinTree, JunctionTree> {

	private JoinTree model;
	private JunctionTree output;

	@Override
	public void setInput(JoinTree model) {
		this.model = model;
	}

	@Override
	public JunctionTree getOutput() {
		return output;
	}

	@Override
	public JunctionTree exec() {
		if (model == null) throw new IllegalArgumentException("No model available");

		output = new JunctionTree();
		model.vertexSet().forEach(output::addVertex);

		for (DefaultWeightedEdge edge : model.edgeSet()) {
			Clique source = model.getEdgeSource(edge);
			Clique target = model.getEdgeTarget(edge);

			int[] variables = ArraysUtil.intersectionSorted(source.getVariables(), target.getVariables());

			Separator S = new Separator(source, target);
			S.setVariables(variables);

			output.addEdge(source, target, S);
		}

		return output;
	}
}
