package ch.idsia.crema.inference.bp.junction;

import ch.idsia.crema.inference.Algorithm;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.join.JoinTree;
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

			output.addEdge(source, target);
		}

		return output;
	}
}
