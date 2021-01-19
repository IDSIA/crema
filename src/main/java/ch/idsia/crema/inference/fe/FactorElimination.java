package ch.idsia.crema.inference.fe;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.Updating;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    07.02.2018 15:50
 */
// TODO: we have three types of FactorElimination... keep just one
public class FactorElimination implements Updating<BayesianFactor, BayesianFactor> {

	private TIntIntMap evidence;
	private EliminationTree tree;

	private int root;

	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	public void setTree(EliminationTree tree) {
		this.tree = tree;
	}

	public void setRoot(int root) {
		this.root = root;
	}

	public Collection<BayesianFactor> FE(int... query) {

		// for each variable E in evidence
		tree.setEvidence(evidence);

		// choose a root node r in the tree T
		tree.setRoot(this.root);

		// collect messages towards root r
		tree.collect();

		// distribute messages away from root r
		tree.distribute();

		// foreach node i compute joint marginal Pr(Ci, e)
		return Arrays.stream(query).mapToObj(i -> tree.compute(i)).collect(Collectors.toList());
	}

	@Override
	public Collection<BayesianFactor> apply(GraphicalModel<BayesianFactor> model, int[] query) {
		return FE(query);
	}

	@Override
	public Collection<BayesianFactor> apply(GraphicalModel<BayesianFactor> model, int[] query, TIntIntMap observations) {
		setEvidence(observations);
		return FE(query);
	}
}
