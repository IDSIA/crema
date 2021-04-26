package ch.idsia.crema;

import ch.idsia.crema.utility.RandomUtil;

import java.util.Random;

public final class CredoArrays {
	private CredoArrays() { }
	
	/**
	 * 	  for i from n − 1 downto 1 do
	 *        j ← random integer such that 0 ≤ j ≤ i
	 *        exchange a[j] and a[i]
	 * @param array
	 */
	public void fisherYatesShuffle(double[] array) {
		Random random = RandomUtil.getRandom();
		
		for (int i = array.length - 1; i > 0; --i) {
			int j = random.nextInt(i + 1);
			double tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}
	
}
