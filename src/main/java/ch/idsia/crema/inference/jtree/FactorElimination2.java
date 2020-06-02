package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.tree.EliminationTree;
import ch.idsia.crema.inference.jtree.tree.Node;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 15:29
 */
public class FactorElimination2 {

	private EliminationTree T;
	private int root;

	public void setEliminationTree(EliminationTree T, int root) {
		this.T = T.copy();
		this.root = root;
	}

	/**
	 * Algorithm 10 from "Modeling and Reasoning with BN", Dawiche, p.156
	 *
	 * @param query variables to query
	 * @return the prior marginal on the query variables
	 */
	public BayesianFactor FE2(int... query) {
		// while T has more than one node
		while (T.size() > 1) {
			// remove a node i =/= r having a single neighbor j from tree T
			Node i = T.remove(root);
			BayesianFactor phi = i.phi();

			int[] Vi = i.vars();
			int[] V = T.missingVariables(Vi);

			// variables appearing in phi_i but not in remaining tree T
			for (int v : V) {
				phi = phi.marginalize(v);
			}

			// update phi_j by combining with phi_i marginalized over V
			Node j = T.getNode(i.getNeighbour().iterator().next());
			BayesianFactor phiJ = j.phi().combine(phi);
			T.addNode(j.getIndex(), phiJ);
		}

		BayesianFactor phiR = T.getNode(root).phi();
		return project(phiR, query);
	}

	/**
	 * Given a {@link BayesianFactor} Fr, marginalize all the variables of this factor that are not equals to the
	 * query variable Q.
	 *
	 * @param phi the target factor
	 * @param Q   the query variable
	 * @return a factor over the query variable
	 */
	private BayesianFactor project(BayesianFactor phi, int... Q) {

		TIntSet variables = new TIntHashSet(phi.getDomain().getVariables());
		for (int q : Q) {
			variables.remove(q);
		}

		for (int v : variables.toArray())
			phi = phi.marginalize(v);

		return phi;
	}

}
