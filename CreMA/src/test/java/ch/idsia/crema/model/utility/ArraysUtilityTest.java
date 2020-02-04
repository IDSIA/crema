package ch.idsia.crema.model.utility;

import ch.idsia.crema.utility.ArraysUtil;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ArraysUtilityTest {


	@Test
	public void testAppendIntArrayIntArray() {
		int[] source = new int[] { 0, 2, 1 };
		int[] other = new int[] { 0, 4, 2 };

		assertArrayEquals(new int[] { 0, 2, 1, 0, 4, 2 }, ArraysUtil.append(source, other));
		assertArrayEquals(source, ArraysUtil.append(source));
		assertArrayEquals(other, ArraysUtil.append(new int[] {}, other));
	}

	@Test
	public void testAppendDoubleArrayDoubleArray() {
		double[] source = new double[] { 0, 2, 1 };
		double[] other = new double[] { 0, 4, 2 };

		assertArrayEquals(new double[] { 0, 2, 1, 0, 4, 2 }, ArraysUtil.append(source, other), 0);
		assertArrayEquals(source, ArraysUtil.append(source), 0);
		assertArrayEquals(other, ArraysUtil.append(new double[] {}, other), 0);
	}

	@Test
	public void testRemoveDouble() {
		double[] source = new double[] { 0, 2, 1 };

		// remove bounds
		assertArrayEquals(new double[] { 2, 1 }, ArraysUtil.remove(source, 0), 0);
		assertArrayEquals(new double[] { 0, 1 }, ArraysUtil.remove(source, 1), 0);
		assertArrayEquals(new double[] { 0, 2 }, ArraysUtil.remove(source, 2), 0);

		// remove only item
		source = new double[] { 1 };
		assertArrayEquals(new double[] {}, ArraysUtil.remove(source, 0), 0);
	}

	@Test
	public void testRemoveInt() {
		int[] source = new int[] { 0, 2, 1 };

		// remove bounds
		assertArrayEquals(new int[] { 2, 1 }, ArraysUtil.remove(source, 0));
		assertArrayEquals(new int[] { 0, 1 }, ArraysUtil.remove(source, 1));
		assertArrayEquals(new int[] { 0, 2 }, ArraysUtil.remove(source, 2));

		// remove only item
		source = new int[] { 1 };
		assertArrayEquals(new int[] {}, ArraysUtil.remove(source, 0));
	}

	@Test
	public void testRemoveTArrayInt() {
		String[] source = new String[] { "0", "2", "1" };
		assertArrayEquals(new String[] { "2", "1" }, ArraysUtil.remove(source, 0));
		assertArrayEquals(new String[] { "0", "1" }, ArraysUtil.remove(source, 1));
		assertArrayEquals(new String[] { "0", "2" }, ArraysUtil.remove(source, 2));

		// remove only item
		source = new String[] { "1" };
		assertArrayEquals(new String[] {}, ArraysUtil.remove(source, 0));
	}

	// @Test
	// public void testOrderIntArrayIntComparator() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOrderIntArray() {
	// fail("Not yet implemented");
	// }
	//
	@Test
	public void testDeepCloneDoubleArrayArray() {
		double[][] values = new double[3][4];
		double[][] newValues = values.clone();

		// simple clone will not copy deeply
		assertTrue(values[0] == newValues[0]);

		newValues[1][1] = 4;

		// so changing the new matrix will affect the old one too.
		assertEquals(values[1][1], newValues[1][1], 0);

		// but main array did change
		assertFalse(values == newValues);

		newValues = ArraysUtil.deepClone(values);

		// deep clone must change also nested arrays
		assertFalse(values[0] == newValues[0]);

		// but main array did change
		assertFalse(values == newValues);

		newValues[2][2] = 4;

		// so changing the new matrix will not affect the old one
		assertNotEquals(values[2][2], newValues[2][2]);
	}

	@Test
	public void testDeepCloneDoubleArrayArrayArray() {
		double[][][] values = new double[3][4][3];

		double[][][] newValues = ArraysUtil.deepClone(values);

		// deep clone must change also nested arrays
		assertFalse(values[0] == newValues[0]);
		// at all levels
		assertFalse(values[0][1] == newValues[0][1]);
		// and main array did change
		assertFalse(values == newValues);

		newValues[2][2][2] = 4;

		// so changing the new matrix will not affect the old one
		assertNotEquals(values[2][2][2], newValues[2][2][2]);
	}

	@Test
	public void testAlmostEqualsDoubleArrayDoubleArrayDouble() {
		assertTrue(ArraysUtil.almostEquals(new double[] { 0.1, 0.2, 0.3 }, new double[] { 0.11, 0.21, 0.31 }, 0.1));

		// size differ
		assertFalse(ArraysUtil.almostEquals(new double[] { 0.1, 0.2 }, new double[] { 0.11, 0.21, 0.31 }, 0.1));
		assertFalse(ArraysUtil.almostEquals(new double[] { 0.1, 0.2, 0.3 }, new double[] { 0.11, 0.21 }, 0.1));

		// difference > eps
		assertFalse(ArraysUtil.almostEquals(new double[] { 0.1, 0.2, 0.3 }, new double[] { 0.11, 0.21 }, 0.09999));

		// zero eps is simple array compare no need to test nulls
		assertTrue(ArraysUtil.almostEquals(new double[] { 0.1, 0.2, 0.3 }, new double[] { 0.1, 0.2, 0.3 }, 0));

		// Null is ok
		assertTrue(ArraysUtil.almostEquals(null, null, 0.00001));

		assertFalse(ArraysUtil.almostEquals(new double[] { 1 }, null, 0.00001));
		assertFalse(ArraysUtil.almostEquals(null, new double[] { 1 }, 0.00001));

		assertTrue(ArraysUtil.almostEquals(new double[] {}, new double[] {}, 0.00001));

		double[] a = new double[] { 1.0, 2.0 };
		assertTrue(ArraysUtil.almostEquals(a, a, 0.00001));

		// NaN (not a number so no compare exists)
		assertFalse(ArraysUtil.almostEquals(new double[] { Double.NaN }, new double[] { Double.NaN }, 0.00001));

		// NaN weirdness that needed special care
		assertFalse(ArraysUtil.almostEquals(new double[] { Double.NaN }, new double[] { 0 }, 0.00001));
		assertFalse(ArraysUtil.almostEquals(new double[] { 0 }, new double[] { Double.NaN }, 0.00001));

		a = new double[] { Double.NaN };
		// a == a so true
		assertTrue(ArraysUtil.almostEquals(a, a, 0.00001));

		// infinite
		assertTrue(ArraysUtil.almostEquals(new double[] { Double.POSITIVE_INFINITY, 0.1 }, new double[] { Double.POSITIVE_INFINITY, 0.1 }, 0.00001));

		assertFalse(ArraysUtil.almostEquals(new double[] { Double.POSITIVE_INFINITY, 0.1 }, new double[] { Double.NEGATIVE_INFINITY, 0.1 }, 0.00001));
		assertFalse(ArraysUtil.almostEquals(new double[] { Double.NEGATIVE_INFINITY, 0.1 }, new double[] { Double.POSITIVE_INFINITY, 0.1 }, 0.00001));
		assertFalse(ArraysUtil.almostEquals(new double[] { 0, 0.1 }, new double[] { Double.POSITIVE_INFINITY, 0.1 }, 0.00001));
		assertFalse(ArraysUtil.almostEquals(new double[] { Double.NEGATIVE_INFINITY, 0.1 }, new double[] { 0, 0.1 }, 0.00001));

	}

	@Test
	public void testAddToSortedArray() {
		int[] all = new int[] { 2, 4, 4, 6, 8, 8 };

		int[] newArray1 = ArraysUtil.addToSortedArray(all, 1);
		assertArrayEquals(new int[] { 1, 2, 4, 4, 6, 8, 8 }, newArray1);

		int[] newArray2 = ArraysUtil.addToSortedArray(all, 2);
		assertArrayEquals(all, newArray2);
		assertTrue(all == newArray2); // original array is returned if value is already part of array

		int[] newArray3 = ArraysUtil.addToSortedArray(all, 4);
		assertArrayEquals(all, newArray3);
		assertTrue(all == newArray3); // original array is returned if value is already part of array

		int[] newArray4 = ArraysUtil.addToSortedArray(all, 8);
		assertArrayEquals(all, newArray4);
		assertTrue(all == newArray4); // original array is returned if value is already part of array

		int[] newArray5 = ArraysUtil.addToSortedArray(all, 14);
		assertArrayEquals(new int[] { 2, 4, 4, 6, 8, 8, 14 }, newArray5);

		int[] newArray6 = ArraysUtil.addToSortedArray(all, 5);
		assertArrayEquals(new int[] { 2, 4, 4, 5, 6, 8, 8 }, newArray6);
	}

	@Test
	public void testRemoveAllFromSortedArray() {
		int[] all = new int[] { 2, 4, 4, 6, 8, 8 };
		int[] els = new int[] { 0, 4 };

		// special cases

		// empty arrays
		assertArrayEquals(all, ArraysUtil.removeAllFromSortedArray(all));
		assertArrayEquals(new int[0], ArraysUtil.removeAllFromSortedArray(new int[0], all));
		assertArrayEquals(new int[0], ArraysUtil.removeAllFromSortedArray(new int[0]));

		// remove all
		assertArrayEquals(new int[0], ArraysUtil.removeAllFromSortedArray(all, all));

		// border cases
		assertArrayEquals(new int[] { 2, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, els));

		// no overlap
		assertArrayEquals(all, ArraysUtil.removeAllFromSortedArray(all, new int[] { 0, 1 }));
		assertArrayEquals(all, ArraysUtil.removeAllFromSortedArray(all, new int[] { 5, 7 }));
		assertArrayEquals(all, ArraysUtil.removeAllFromSortedArray(all, new int[] { 9, 10 }));

		// first
		assertArrayEquals(new int[] { 4, 4, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 2 }));
		assertArrayEquals(new int[] { 4, 4, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 2, 3 }));
		assertArrayEquals(new int[] { 4, 4, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 0, 1, 2 }));
		assertArrayEquals(new int[] { 4, 4, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 0, 1, 2, 3 }));
		assertArrayEquals(new int[] { 4, 4, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 0, 1, 2, 3, 9 }));

		// last
		assertArrayEquals(new int[] { 2, 4, 4, 6 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 8 }));
		assertArrayEquals(new int[] { 2, 4, 4, 6 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 8, 9 }));
		assertArrayEquals(new int[] { 2, 4, 4, 6 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 7, 8, 9 }));
		assertArrayEquals(new int[] { 2, 4, 4, 6 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 0, 8, 9 }));

		// remove dups
		assertArrayEquals(new int[] { 2, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 4 }));
		assertArrayEquals(new int[] { 2, 6, 8, 8 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 4, 4, 4 }));
		assertArrayEquals(new int[] { 2, 6 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 4, 8 }));
		assertArrayEquals(new int[] { 2, 6 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 4, 4, 4, 8, 8, 8, 8 }));

		// leave
		assertArrayEquals(new int[] { 4, 4 }, ArraysUtil.removeAllFromSortedArray(all, new int[] { 2, 6, 8 }));

	}

	@Test
	public void testroundArrayToTarget() {
		double[] a = {0.1, 0.2, 0.699999};
		double[] b = {0.5, 0.2, 0.31};
		double[] c = {0.5, 0.3, 0.4};

		double eps = 0.1;
		double target = 1.0;

		ArraysUtil.roundArrayToTarget(a, target, eps);
		ArraysUtil.roundArrayToTarget(b, target, eps);
		ArraysUtil.roundArrayToTarget(c, target, eps);

		double sumA = Arrays.stream(a).sum();
		double sumB = Arrays.stream(b).sum();
		double sumC = Arrays.stream(c).sum();

		assertEquals(target, sumA, 0.0);
		assertEquals(target, sumB, 0.0);
		assertNotEquals(target, sumC, 0.0);
	}

}
