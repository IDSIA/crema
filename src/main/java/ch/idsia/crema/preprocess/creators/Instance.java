package ch.idsia.crema.preprocess.creators;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.03.2021 18:42
 */
public class Instance implements Comparable<Instance> {
	public final int variable;
	public final int size;
	public final int observed;

	public Instance(int variable, int size, int observed) {
		this.variable = variable;
		this.size = size;
		this.observed = observed;
	}

	@Override
	public int compareTo(Instance o) {
		return variable - o.variable;
	}
}
