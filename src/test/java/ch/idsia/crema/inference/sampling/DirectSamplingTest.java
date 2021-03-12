package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.inference.BayesianNetworkContainer;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:05
 */
public class DirectSamplingTest {

	BayesianNetwork model;

	@BeforeEach
	public void setUp() {
		BayesianNetworkContainer BN = BayesianNetworkContainer.mix5Variables();

		model = BN.network;
	}

	@Test
	public void testSamplingIsWorking() {
		DirectSampling ds = new DirectSampling();

		System.out.println("P(Rain) =                                     " + ds.query(model, 2));
	}

	@Test
	public void testSamplingRaiseException() {
		DirectSampling ds = new DirectSampling();

		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			TIntIntMap evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1});
			System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + ds.query(model, evidence, 2));
		});

		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			TIntIntMap evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0});
			System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + ds.query(model, evidence, 2));
		});

		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			TIntIntMap evidence = new TIntIntHashMap(new int[]{0}, new int[]{1});
			System.out.println("P(Rain|Winter = true) =                       " + ds.query(model, evidence, 2));
		});

		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			TIntIntMap evidence = new TIntIntHashMap(new int[]{0}, new int[]{0});
			System.out.println("P(Rain|Winter = false) =                      " + ds.query(model, evidence, 2));
		});
	}
}