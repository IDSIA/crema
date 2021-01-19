package ch.idsia.crema.inference.bp.junction;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.Algorithm;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.join.JoinTree;
import ch.idsia.crema.utility.ArraysUtil;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 15:19
 */
public class JunctionTreeBuilder<F extends Factor<F>> implements Algorithm<JoinTree, JunctionTree<F>> {

	private JoinTree model;
	private JunctionTree<F> output;

	@Override
	public void setInput(JoinTree model) {
		this.model = model;
	}

	@Override
	public JunctionTree<F> getOutput() {
		return output;
	}

	@Override
	public JunctionTree<F> exec() {
		if (model == null) throw new IllegalArgumentException("No model available");

		output = new JunctionTree<>();
		model.vertexSet().forEach(output::addVertex);

		for (DefaultWeightedEdge edge : model.edgeSet()) {
			Clique source = model.getEdgeSource(edge);
			Clique target = model.getEdgeTarget(edge);

			int[] variables = ArraysUtil.intersectionSorted(source.getVariables(), target.getVariables());

			Separator<F> S = new Separator<>(source, target);
			S.setVariables(variables);

			output.addEdge(source, target, S);
		}

		return output;
	}
}
