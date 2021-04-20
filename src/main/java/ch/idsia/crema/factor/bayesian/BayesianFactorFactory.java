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

	private boolean log = false;

	private Strides domain = Strides.empty();

	private BayesianFactorFactory() {
	}

	public static BayesianFactorFactory factory() {
		return new BayesianFactorFactory();
	}

	public BayesianFactorFactory log() {
		log = true;
		return this;
	}

	public BayesianFactorFactory domain(Domain domain) {
		this.domain = Strides.fromDomain(domain);
		return this;
	}

	public BayesianFactorFactory domain(int[] domain, int[] sizes) {
		this.domain = new Strides(domain, sizes);
		return this;
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

	public BayesianFactor build() {
		if (log) {
			return new BayesianLogFactor(domain, data);
		} else {
			return new BayesianDefaultFactor(domain, data);
		}
	}

}
