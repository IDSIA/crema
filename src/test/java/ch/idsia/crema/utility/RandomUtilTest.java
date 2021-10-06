package ch.idsia.crema.utility;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    06.10.2021 10:24
 */
class RandomUtilTest {

	@AfterEach
	void tearDown() {
		RandomUtil.reset();
	}

	@Test
	void testRandomSupplier() {

		final Random r1 = new Random(1); // first nextInt(): -1155869325
		final Random r2 = new Random(2); // first nextInt(): -1154715079

		RandomUtil.setRandom(r1);

		Assertions.assertEquals(-1155869325, RandomUtil.getRandom().nextInt());

		RandomUtil.setRandom(() -> r2);

		Assertions.assertEquals(-1154715079, RandomUtil.getRandom().nextInt());
	}

	@Test
	void testConcurrentRandomExecution() throws Exception {
		final ExecutorService es = Executors.newFixedThreadPool(2);

		final Map<String, Random> randoms = new ConcurrentHashMap<>();

		RandomUtil.setRandom(() -> randoms.get(Thread.currentThread().getName()));

		final List<Callable<int[]>> tasks = IntStream.range(0, 4)
				.mapToObj(i -> (Callable<int[]>) () -> {
					final int seed = i % 2;
					final String tName = "Thread" + seed;
					Thread.currentThread().setName(tName);
					randoms.put(tName, new Random(seed));

					return IntStream.generate(() -> RandomUtil.getRandom().nextInt(10))
							.limit(10)
							.toArray();
				})
				.collect(Collectors.toList());

		final List<Future<int[]>> futures = es.invokeAll(tasks);

		es.shutdown();

		final int[] seq0 = futures.get(0).get();
		final int[] seq1 = futures.get(1).get();
		final int[] seq2 = futures.get(2).get();
		final int[] seq3 = futures.get(3).get();

		Assertions.assertArrayEquals(seq0, seq2);
		Assertions.assertArrayEquals(seq1, seq3);
	}
}