package ch.idsia.crema.utility;

import java.util.Random;


public class RandomUtil {

	private static Random random = new Random(1234);

	/**
	 * Sample of vector where the sum of all its elements is 1
	 *
	 * @param size
	 * @return
	 */
	public static double[] sampleNormalized(int size) {
		final double[] probs = new double[size];

		double sum = 0;
		for (int i = 0; i < size; i++) {
			probs[i] = random.nextDouble();
			sum += probs[i];
		}

		double norm = 0;
		for (int i = 0; i < size - 1; i++) {
			probs[i] /= sum;
			norm += probs[i];
		}

		probs[size - 1] = 1.0 - norm;
		ArraysUtil.shuffle(probs);

		return probs;
	}

	public static int sampleCategorical(double[] probs) {
		final double x = random.nextDouble();

		double sum = 0;
		for (int i = 0; i < probs.length; i++) {
			sum += probs[i];
			if (x < sum) {
				return i;
			}
		}

		return probs.length - 1;
	}

	public static void setRandom(Random random) {
		RandomUtil.random = random;
	}

	public static void setRandomSeed(long seed) {
		setRandom(new Random(seed));
	}

	public static Random getRandom() {
		return random;
	}
}
