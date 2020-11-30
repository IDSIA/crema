package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.BayesianNetworkContainer;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collection;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    02.02.2018 09:17
 */
public class StochasticSamplingTest {

	BayesianNetwork model;

	@Before
	public void setUp() {
		BayesianNetworkContainer BN = BayesianNetworkContainer.mix5Variables();

		model = BN.network;
	}

	public String factorsToString(Collection<BayesianFactor> factors) {
		StringBuilder sb = new StringBuilder();
		for (BayesianFactor factor : factors) {
			sb.append(Arrays.toString(factor.getData()));
		}

		return sb.toString();
	}
}