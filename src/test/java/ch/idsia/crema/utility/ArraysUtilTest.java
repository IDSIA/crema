package ch.idsia.crema.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ArraysUtilTest {

	@Test
	void testAddToSortedArray() {
		assertArrayEquals(new int[]{1}, ArraysUtil.addToSortedArray(new int[]{}, 1));
		assertArrayEquals(new int[]{1, 2}, ArraysUtil.addToSortedArray(new int[]{1}, 2));
		assertArrayEquals(new int[]{1, 2}, ArraysUtil.addToSortedArray(new int[]{2}, 1));
		assertArrayEquals(new int[]{1, 2, 3}, ArraysUtil.addToSortedArray(new int[]{2, 3}, 1));
		assertArrayEquals(new int[]{1, 2, 3}, ArraysUtil.addToSortedArray(new int[]{1, 2}, 3));

		// no duplicates are added 
		assertArrayEquals(new int[]{1, 2}, ArraysUtil.addToSortedArray(new int[]{1, 2}, 2));
		assertArrayEquals(new int[]{1, 2}, ArraysUtil.addToSortedArray(new int[]{1, 2}, 1));

		// but they are maintained if already there 
		assertArrayEquals(new int[]{1, 1, 2}, ArraysUtil.addToSortedArray(new int[]{1, 1}, 2));
		assertArrayEquals(new int[]{1, 2, 2}, ArraysUtil.addToSortedArray(new int[]{2, 2}, 1));

		// normal case
		assertArrayEquals(new int[]{1, 3, 5}, ArraysUtil.addToSortedArray(new int[]{1, 5}, 3));
	}

	@Test
	void testAt() {
		assertArrayEquals(new int[]{}, ArraysUtil.at(new int[]{}, new int[]{}));
		assertArrayEquals(new int[]{}, ArraysUtil.at(new int[]{1, 2, 3, 4}, new int[]{}));
		assertArrayEquals(new int[]{2, 2, 3}, ArraysUtil.at(new int[]{1, 2, 3, 4}, new int[]{1, 1, 2}));
		assertArrayEquals(new int[]{1, 2, 3, 4, 1, 2, 3, 4},
				ArraysUtil.at(new int[]{1, 2, 3, 4}, new int[]{0, 1, 2, 3, 0, 1, 2, 3}));
	}

	@Test
	void testReverse() {
		assertArrayEquals(new int[]{4, 3, 2, 1}, ArraysUtil.reverse(new int[]{1, 2, 3, 4}));
		assertArrayEquals(new int[]{}, ArraysUtil.reverse(new int[]{}));
	}
}
