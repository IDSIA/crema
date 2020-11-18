package ch.idsia.crema.inference.jtree.algorithm.junction;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:25
 */
public class Separator<F extends Factor<F>> {

	private int[] variables;

	private final Clique source;
	private final Clique target; // separator is always between two cliques

	private F messageFromSource;
	private F messageFromTarget;


	public Separator(Clique source, Clique target) {
		this.source = source;
		this.target = target;
		variables = source.intersection(target);
	}

	public Clique getSource() {
		return source;
	}

	public Clique getTarget() {
		return target;
	}

	public void setVariables(int[] variables) {
		this.variables = variables;
	}

	public int[] getVariables() {
		return variables;
	}

	public void setMessage(Clique from, F message) {
		if (from.equals(source))
			messageFromSource = message;
		else
			messageFromTarget = message;
	}

	public F getMessage(Clique from) {
		if (from.equals(source))
			return messageFromSource;
		return messageFromTarget;
	}

	@Override
	public String toString() {
		return "S{" + Arrays.toString(variables) + "}";
	}
}
