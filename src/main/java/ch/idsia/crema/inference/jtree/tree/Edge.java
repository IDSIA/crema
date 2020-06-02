package ch.idsia.crema.inference.jtree.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:30
 */
public class Edge {

	int start;
	int end;

	BayesianFactor messageIn;
	BayesianFactor messageOut;

	public Edge(int start, int end) {
		this.start = start;
		this.end = end;
	}
}
