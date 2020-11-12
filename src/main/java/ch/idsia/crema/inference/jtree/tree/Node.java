package ch.idsia.crema.inference.jtree.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.HashSet;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:29
 */
public class Node {
	private final int index;

	private BayesianFactor factor;

	private final Set<Integer> neighbour = new HashSet<>();
	private final Set<Node> incoming = new HashSet<>();

	public Node(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setFactor(BayesianFactor factor) {
		this.factor = factor;
	}

	public Set<Integer> getNeighbour() {
		return neighbour;
	}

	public BayesianFactor phi() {
		return factor;
	}

	public int[] vars() {
		return phi().getDomain().getVariables();
	}

	public BayesianFactor getMessage() {
		BayesianFactor phi = phi();

		for (Node n : incoming) {
			phi = phi.combine(n.getMessage());
		}

		return phi;
	}

	@Override
	public String toString() {
		return "#" + index + " (" + factor + ')';
	}
}
