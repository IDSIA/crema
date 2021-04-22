package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 11:11
 */
public class BayesianFactorFactory {
	private double[] data = null;

	private Strides domain = Strides.empty();

	private BayesianFactorFactory() {
	}

	public static BayesianFactorFactory factory() {
		return new BayesianFactorFactory();
	}

	public BayesianFactorFactory domain(Domain domain) {
		this.domain = Strides.fromDomain(domain);
		return data();
	}

	public BayesianFactorFactory domain(int[] domain, int[] sizes) {
		this.domain = new Strides(domain, sizes);
		return data();
	}

	public BayesianFactorFactory data() {
		this.data = new double[domain.getCombinations()];
		return this;
	}

	public BayesianFactorFactory data(double[] data) {
		final int expectedLength = new double[domain.getCombinations()].length;
		if (data.length != expectedLength)
			throw new IllegalArgumentException("Invalid length of data: expected " + expectedLength + " got " + data.length);

		this.data = data;
		return this;
	}

	public BayesianFactorFactory value(double value, int... states) {
		data[domain.getOffset(states)] = value;
		return this;
	}

	public BayesianFactorFactory valueAt(double d, int index) {
		data[index] = d;
		return this;
	}

	public BayesianLogFactor log() {
		return new BayesianLogFactor(domain, data);
	}

	public BayesianDefaultFactor get() {
		return new BayesianDefaultFactor(domain, data);
	}

}
