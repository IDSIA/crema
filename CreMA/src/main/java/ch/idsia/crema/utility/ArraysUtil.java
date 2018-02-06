package ch.idsia.crema.utility;

import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class ArraysUtil {

	public static void reorder(double[] data, int[] order) {
		for (int index = 0; index < order.length;++index) {
			double o = data[order[index]];
			data[order[index]]= data[index];
			data[index] = o;
		}
	}

	public static void reorder(int[] data, int[] order) {
		for (int index = 0; index < order.length;++index) {
			int o = data[order[index]];
			data[order[index]]= data[index];
			data[index] = o;
		}
	}

	/**
	 * Append element of second to the end of base and return the new array.
	 *
	 * @param base
	 *            original items (Not Null)
	 * @param second
	 *            elements to be added to base (Not Null)
	 * @return the new array with second appended to base
	 */
	public static int[] append(int[] base, int... second) {
		int[] result = new int[base.length + second.length];
		System.arraycopy(base, 0, result, 0, base.length);
		System.arraycopy(second, 0, result, base.length, second.length);
		return result;
	}

	/**
	 * Append element of second to the end of base and return the new array.
	 *
	 * @param base
	 *            original items (Not Null)
	 * @param second
	 *            elements to be added to base (Not Null)
	 * @return the new array with second appended to base
	 */
	public static double[] append(double[] base, double... second) {
		double[] result = new double[base.length + second.length];
		System.arraycopy(base, 0, result, 0, base.length);
		System.arraycopy(second, 0, result, base.length, second.length);
		return result;
	}

	/**
	 * Remove the element at the specified index from the base array.
	 *
	 * @param base
	 *            original items (Not Null)
	 * @param index
	 *            the index of the element to be removed
	 * @return the new array
	 */
	public static double[] remove(double[] base, int index) {
		int new_size = base.length - 1;
		double[] result = new double[new_size];
		System.arraycopy(base, 0, result, 0, index);
		System.arraycopy(base, index + 1, result, index, new_size - index);
		return result;
	}

	/**
	 * Remove the element at the specified index from the base array.
	 *
	 * @param base
	 *            original items (Not Null)
	 * @param index
	 *            the index of the element to be removed
	 * @return the new array
	 */
	public static int[] remove(int[] base, int index) {
		int new_size = base.length - 1;
		int[] result = new int[new_size];
		System.arraycopy(base, 0, result, 0, index);
		System.arraycopy(base, index + 1, result, index, new_size - index);
		return result;
	}

	/**
	 * Remove the element at the specified index from the base array.
	 *
	 * @param base
	 *            original items (Not Null)
	 * @param index
	 *            the index of the element to be removed
	 * @return the new array
	 */
	public static <T> T[] remove(T[] base, int index) {
		int new_size = base.length - 1;
		T[] result = java.util.Arrays.copyOf(base, new_size);
		System.arraycopy(base, index + 1, result, index, new_size - index);
		return result;
	}

	public static int[] sort(int[] base) {
		int[] copy = base.clone();
		Arrays.sort(copy);
		return copy;
	}

    private static class X {
		public final int pos;
		public final int val;

		public X(int pos, int val) {
			this.pos = pos;
			this.val = val;
		}
	}

	public static int[] order(int[] data, final IntComparator comparator) {
		ArrayList<X> internal = new ArrayList<>(data.length);
		for (int i = 0; i < data.length; ++i) {
			internal.add(new X(i, data[i]));
		}

		Collections.sort(internal, new Comparator<X>() {
			@Override
			public int compare(X o1, X o2) {
				return comparator.compare(o1.val, o2.val);
			}
		});

		int[] positions = new int[data.length];
		for (int i = 0; i < data.length; ++i) {
			X x = internal.get(i);
			positions[i] = x.pos;
			data[i] = x.val;
		}

		return positions;
	}

	public static int[] order(int[] data) {
		ArrayList<X> internal = new ArrayList<>(data.length);
		for (int i = 0; i < data.length; ++i) {
			internal.add(new X(i, data[i]));
		}

		Collections.sort(internal, new Comparator<X>() {
			@Override
			public int compare(X o1, X o2) {
				return o1.val - o2.val;
			}
		});

		int[] positions = new int[data.length];
		for (int i = 0; i < data.length; ++i) {
			X x = internal.get(i);
			positions[i] = x.pos;
			data[i] = x.val;
		}

		return positions;
	}

	public static double[][] deepClone(double[][] data) {
		double[][] result = new double[data.length][];
		for (int i = 0; i < data.length; ++i) {
			result[i] = data[i].clone();
		}
		return result;
	}

	public static double[][][] deepClone(double[][][] data) {
		double[][][] result = new double[data.length][][];
		for (int i = 0; i < data.length; ++i) {
			result[i] = deepClone(data[i]);
		}
		return result;
	}

	/**
	 * Compare two float arrays for almost equality. To be equal, each pair of
	 * items of the arrays can differ at most by eps.
	 *
	 * @param one
	 *            the first array
	 * @param two
	 *            the second array
	 * @param eps
	 *            the tollerance
	 * @return true when the arrays are almost equal
	 */
	public static boolean almostEquals(float[] one, float[] two, float eps) {
		if (eps == 0)
			return Arrays.equals(one, two);

		if (one == two)
			return true;
		if (one == null || two == null || one.length != two.length)
			return false;
		for (int index = 0; index < one.length; ++index) {
			if (Math.abs(one[index] - two[index]) >= eps)
				return false;
		}
		return true;
	}

	/**
	 * Compare two double arrays for almost equality. To be equal, each pair of
	 * items of the arrays can differ at most by eps.
	 *
	 * @param one
	 *            the first array (null is ok, NaN not!)
	 * @param two
	 *            the second array (null is ok, NaN not!)
	 * @param eps
	 *            the tolerance (0 is ok)
	 * @return true when the arrays are almost equal
	 */
	public static boolean almostEquals(double[] one, double[] two, double eps) {
		if (eps == 0)
			return Arrays.equals(one, two);

		if (one == two)
			return true;

		if (one == null || two == null || one.length != two.length)
			return false;

		for (int index = 0; index < one.length; ++index) {
			double diff = Math.abs(one[index] - two[index]);
			// diff > eps is true when diff is NaN (one xor two is NaN)
			// but it is ok for both to be NaN
			if (one[index] != two[index] && (diff >= eps || Double.isNaN(diff)))
				return false;
		}
		return true;
	}

	/**
	 * remove the element from the array assuming the array is sorted. The
	 * removal is done keeping the ordering. The method returns the new array.
	 *
	 * @param array
	 *            a sorted array (read-only)
	 * @param element
	 *            the element to be removed
	 * @return the new array or the original array if the element was not found
	 */
	public static int[] removeFromSortedArray(int[] array, int element) {

		if (array == null || array.length == 0) {
			// when no items in the array then return a new array with only the
			// item
			return array;
		}

		// single item array
		if (array.length == 1) {
			// if the only item of the array is the variable we can return null
			return array[0] == element ? ArrayUtils.EMPTY_INT_ARRAY : array;
		}

		// look for existing links
		int pos = Arrays.binarySearch(array, element);
		if (pos >= 0) { // else variable is not in the array
			// make the list smaller
			int[] new_array = Arrays.copyOf(array, array.length - 1);
			if (pos < new_array.length) {
				//				System.out.println(array.length  +" "+(pos + 1)+" "+ new_array.length+" "+ pos+" "+ (array.length - 1));
				System.arraycopy(array, pos + 1, new_array, pos, array.length - pos - 1);
			}

			// persist changes
			return new_array;
		}
		return array;
	}

	/**
	 * Add the element to a sorted array. The method return a new sorted array
	 * containing the specified element. The original array is returned if the
	 * element was already part of the array.
	 *
	 * @param array
	 *            the sorted array
	 * @param element
	 *            the item to be added to the array
	 * @return a new array containing the element or the original one if element
	 *         is already present
	 */
	public static int[] addToSortedArray(int[] array, int element) {
		if (array == null || array.length == 0) {
			// when no items in the array then return a new array with only the
			// item
			return new int[] { element };
		}

		// look for existing links
		int pos = Arrays.binarySearch(array, element);
		if (pos < 0) {
			// convert to the index where the item should be inserted
			pos = -(pos + 1);

			// make the list longer
			int[] new_array = Arrays.copyOf(array, array.length + 1);
			System.arraycopy(array, pos, new_array, pos + 1, array.length - pos);

			// and place the new child node
			new_array[pos] = element;

			// persist changes
			return new_array;
		} // else there is already such a variable in the array
		return array;
	}

	/**
	 * Remove the elements from the array assuming both arrays are sorted and
	 * not null
	 *
	 * @param array
	 * @param elements
	 * @return
	 */
	public static int[] removeAllFromSortedArray(int[] array, int... elements) {
		if (array.length == 0 || elements.length == 0)
			return array;

		TIntArrayList target = new TIntArrayList(Math.max(0, array.length - elements.length));
		int array_index = 0;
		int elements_index = 0;

		while (array_index < array.length && elements_index < elements.length) {
			int compare = array[array_index] - elements[elements_index];
			if (compare == 0) {
				++array_index;
			} else if (compare > 0) {
				++elements_index;
			} else { // compare < 0
				target.add(array[array_index]);
				++array_index;
			}
		}

		for (; array_index < array.length; ++array_index) {
			target.add(array[array_index]);
		}

		return target.toArray();
	}

	/**
	 * Union of two sorted arrays.
	 * @param arr1
	 * @param arr2
	 * @return
	 */
	public static int[] union(int[] arr1, int[] arr2) {
		final int s1 = arr1.length;
		final int s2 = arr2.length;

		// size the target arrays assuming no overlap
		final int max  = s1 + s2;
		int[] arr_union = new int[max];

		// (pt1) c1 and c2 are the positions in the two domains
		int c1 = 0;
		int c2 = 0;

		int t = 0;
		while(c1 < s1 && c2 < s2) {
			int v1 = arr1[c1];
			int v2 = arr2[c2];

			if (v1 < v2) {
				arr_union[t] = v1;
				c1++;
			} else if(v1 > v2) {
				arr_union[t] = v2;
				c2++;
			} else {
				arr_union[t] = v1;
				c1++;
				c2++;
			}
			++t;
		}

		// (pt2) check if there is one domain not completely copied that can be 
		// moved over in bulk.
		if (c1 < s1) {
			System.arraycopy(arr1, c1, arr_union, t, s1 - c1);
			t += s1 - c1;
		} else if (c2 < s2) {
			System.arraycopy(arr2, c2, arr_union, t, s2 - c2);
			t += s2 - c2;
		}

		// fix array sizes if there was overlap (we assumed no overlap while sizing)
		if (t < max) {
			arr_union = Arrays.copyOf(arr_union, t);
		}
		return arr_union;
	}

	/**
	 * Find the sorted intersection of two sorted integer arrays.
	 *
	 * @param arr1 the first sorted array
	 * @param arr2 the second sorted array
	 * @return a sorted array intersection of the first two
	 */
	public static int[] intersection(int[] arr1, int[] arr2) {
		final int s1 = arr1.length;
		final int s2 = arr2.length;

		// size the target arrays assuming no overlap
		final int max  = FastMath.min(s1, s2);
		int[] arr_union = new int[max];

		// (pt1) c1 and c2 are the positions in the two arrays
		int c1 = 0;
		int c2 = 0;

		int t = 0;
		while(c1 < s1 && c2 < s2) {
			int v1 = arr1[c1];
			int v2 = arr2[c2];

			if (v1 < v2) {
				c1++;
			} else if(v1 > v2) {
				c2++;
			} else {
				arr_union[t++] = v1;
				c1++;
				c2++;
			}
		}

		// fix array size
		if (t < max) {
			arr_union = Arrays.copyOf(arr_union, t);
		}
		return arr_union;
	}

	/**
	 * Normalize an array by fixing the last value of the array so that it sums up to the target value.
	 * @param arr array to normalize
	 * @param target target sum value
	 * @param eps tolerance threshold
	 */
	public static void roundArrayToTarget(double[] arr, double target, double eps) {
		double sum = Arrays.stream(arr).sum();
		if(sum!=target)
			if (Math.abs(sum-target)<eps)
				if(sum>target)
					arr[arr.length-1] -= (sum-target);
				else
					arr[arr.length-1] += (target-sum);
	}

	/**
	 * Check if the needle value is contained inside the haystack array.
	 * @param needle what to search
	 * @param haystack where to search
	 * @return true if the array contains the element, otherwise false
	 */
	public static boolean contains(int needle, int[] haystack) {
		for (int hay : haystack) {
			if (hay == needle)
				return true;
		}
		return false;
	}
}

