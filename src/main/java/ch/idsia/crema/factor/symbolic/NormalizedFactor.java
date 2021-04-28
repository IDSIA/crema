package ch.idsia.crema.factor.symbolic;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    28.04.2021 16:36
 */
public class NormalizedFactor extends SymbolicAbstractFactor {

	private final SymbolicFactor factor;
	private final int[] given;

	public NormalizedFactor(SymbolicFactor factor, int... given) {
		super(factor.getDomain());

		this.factor = factor;
		this.given = given;
	}

	@Override
	public NormalizedFactor copy() {
		return new NormalizedFactor(factor, given);
	}

	public SymbolicFactor getFactor() {
		return factor;
	}

	public int[] getGiven() {
		return given;
	}

	@Override
	public SymbolicFactor[] getSources() {
		return new SymbolicFactor[]{factor};
	}

	@Override
	public String toString() {
		if (given.length > 1)
			return String.format("%s.normalize(%s)", factor, Arrays.toString(given));
		return String.format("%s.normalize()", factor);
	}
}
