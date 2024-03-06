package ch.idsia.crema.utility;

import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;


public class RandomUtil {

	private static Random random = new Random(1234);

	private static Supplier<Random> supplier = () -> random;

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
			probs[i] = getRandom().nextDouble();
			sum += probs[i];
		}

		double norm = 0;
		for (int i = 0; i < size - 1; i++) {
			probs[i] /= sum;
			norm += probs[i];
		}

		probs[size - 1] = 1.0 - norm;
		ArrayUtils.shuffle(probs);

		return probs;
	}

	public static int sampleCategorical(double[] probs) {
		final double x = getRandom().nextDouble();

		double sum = 0;
		for (int i = 0; i < probs.length; i++) {
			sum += probs[i];
			if (x < sum) {
				return i;
			}
		}

		return probs.length - 1;
	}

	/**
	 * Set a new {@link #random} object to use and reset the supplier function to the original one.
	 *
	 * @param random the {@link Random} object to use.
	 */
	public static void setRandom(Random random) {
		RandomUtil.random = random;
		setRandom(() -> random);
	}

	/**
	 * Change the current {@link #supplier} function with a new one.
	 * <p>
	 * Note that <b>ALL</b> future call to the {@link #getRandom()} method will use this function!
	 *
	 * @param random new supplier of {@link Random} objects
	 */
	public static void setRandom(Supplier<Random> random) {
		supplier = random;
	}

	/**
	 * Change the current {@link #random} object with a new random initialized with the given seed.
	 *
	 * @param seed new random seed to use
	 */
	public static void setRandomSeed(long seed) {
		setRandom(new Random(seed));
	}

	/**
	 * @return a {@link Random} object based on the current {@link #supplier} function.
	 */
	public static Random getRandom() {
		return supplier.get();
	}

	/**
	 * Set the {@link #supplier} function back to the original one.
	 */
	public static void reset() {
		setRandom(() -> random);
	}

}
