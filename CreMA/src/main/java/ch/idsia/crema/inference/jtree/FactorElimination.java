package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.tree.EliminationTree;
import gnu.trove.map.TIntIntMap;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    07.02.2018 15:50
 */
public class FactorElimination {

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

	public BayesianFactor[] FE(int... query) {

		// for each variable E in evidence
		tree.setEvidence(evidence);

		// choose a root node r in the tree T
		tree.setRoot(this.root);

		// collect messages towards root r
		tree.collect();

		// distribute messages away from root r
		tree.distribute();

		// foreach node i compute joint marginal Pr(Ci, e)

		for (int i : tree.getNodes()) {
			// TODO
			BayesianFactor Pr = tree.compute(i);
			System.out.println(i + " " + Arrays.toString(Pr.getData()));
		}

		return null;
	}
}
