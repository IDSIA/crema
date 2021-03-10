package ch.idsia.crema.inference.sampling;

import gnu.trove.map.TIntIntMap;
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

		TIntIntMap evidence = new TIntIntHashMap();
		System.out.println("P(Rain) =                                     " + ds.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 1});
		System.out.println("P(Rain|Wet Grass = false, Slippery = true) =  " + ds.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{3, 4}, new int[]{0, 0});
		System.out.println("P(Rain|Wet Grass = false, Slippery = false) = " + ds.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{1});
		System.out.println("P(Rain|Winter = true) =                       " + ds.query(model, evidence, 2));

		evidence = new TIntIntHashMap(new int[]{0}, new int[]{0});
		System.out.println("P(Rain|Winter = false) =                      " + ds.query(model, evidence, 2));
	}
}