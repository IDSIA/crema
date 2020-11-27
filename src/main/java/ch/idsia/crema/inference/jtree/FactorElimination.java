package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.Updating;
import ch.idsia.crema.inference.jtree.tree.EliminationTree;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    07.02.2018 15:50
 */
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

		List<BayesianFactor> factors = new ArrayList<>();

		for (int i : query) {
			// TODO
			BayesianFactor Pr = tree.compute(i);
			factors.add(Pr);
			System.out.println(i + " " + Arrays.toString(Pr.getData()));
		}

		return factors;
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
