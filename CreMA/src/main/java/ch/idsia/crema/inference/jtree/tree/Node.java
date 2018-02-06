package ch.idsia.crema.inference.jtree.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:29
 */
public class Node {
	int i;

	BayesianFactor factors;

	List<Node> neighbour = new ArrayList<>();


	public Node(int i) {
		this.i = i;
	}

	public void setFactor(BayesianFactor factor) {
		this.factors = factor;
	}

	public void addNeighbour(Node n) {
		neighbour.add(n);
	}

	public BayesianFactor phi() {
		return factors;
	}

	public int[] vars() {
		return phi().getDomain().getVariables();
	}

	public List<Node> neighbours() {
		return neighbour;
	}

	public int index() {
		return i;
	}
}
