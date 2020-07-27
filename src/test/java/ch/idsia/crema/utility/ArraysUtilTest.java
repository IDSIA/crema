package ch.idsia.crema.utility;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArraysUtilTest {


	@Test
	void testAt() {
		assertArrayEquals(new int[] { }, ArraysUtil.at(new int[] { }, new int[] { }));
		assertArrayEquals(new int[] { }, ArraysUtil.at(new int[] { 1,2,3,4 }, new int[] { }));
		assertArrayEquals(new int[] {2, 2, 3}, ArraysUtil.at(new int[] { 1,2,3,4 }, new int[] {1, 1, 2}));
		assertArrayEquals(new int[] {1, 2, 3, 4, 1, 2, 3, 4}, ArraysUtil.at(new int[] { 1,2,3,4 }, new int[] {0, 1, 2, 3, 0, 1, 2, 3}));
	}

	@Test
	void testReverse() {
		assertArrayEquals(new int[] { 4,3,2,1 }, ArraysUtil.reverse(new int[] { 1,2,3,4 }));
		assertArrayEquals(new int[] { }, ArraysUtil.reverse(new int[] {} ));
	}
}
