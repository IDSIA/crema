package ch.idsia.crema.inference.sampling;

import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Test;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:05
 */
public class DirectSamplingTest extends StochasticSamplingTest {

	@Test
	public void run() {
		DirectSampling ds = new DirectSampling();
		ds.setModel(model);

		ds.setEvidence(new TIntIntHashMap());
		System.out.println("P(Rain) =                                     " + factorsToString(ds.run(2)));

		ds.setEvidence(new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1}));
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + factorsToString(ds.run(2)));

		ds.setEvidence(new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0}));
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + factorsToString(ds.run(2)));

		ds.setEvidence(new TIntIntHashMap(new int[]{0}, new int[]{1}));
		System.out.println("P(Rain|Winter = true) =                       " + factorsToString(ds.run(2)));

		ds.setEvidence(new TIntIntHashMap(new int[]{0}, new int[]{0}));
		System.out.println("P(Rain|Winter = false) =                      " + factorsToString(ds.run(2)));
	}
}