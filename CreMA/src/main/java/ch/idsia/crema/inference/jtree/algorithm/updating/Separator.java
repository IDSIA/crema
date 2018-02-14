package ch.idsia.crema.inference.jtree.algorithm.updating;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:25
 */
public class Separator {

	private int[] variables;

	private Clique source;
	private Clique target;

	private BayesianFactor messageFromSource;
	private BayesianFactor messageFromTarget;

	public Separator(Clique source, Clique target) {
		this.source = source;
		this.target = target;

		variables = source.intersection(target);
	}

	public void setMessage(Clique from, BayesianFactor message) {
		if (from.equals(source))
			messageFromSource = message;
		else
			messageFromTarget = message;
	}

	public BayesianFactor getMessage(Clique from) {
		if (from.equals(source))
			return messageFromSource;
		return messageFromTarget;
	}

	public int[] getVariables() {
		return variables;
	}

	@Override
	public String toString() {
		return "S{" + source + ", " + target + "}: " + Arrays.toString(variables);
	}
}
