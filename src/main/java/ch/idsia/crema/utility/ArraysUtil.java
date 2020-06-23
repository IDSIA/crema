package ch.idsia.crema.utility;

import ch.idsia.crema.model.Strides;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
	 * Find the sorted intersection of two non-sorted integer arrays.
	 *
	 * @param arr1 the first  array
	 * @param arr2 the second  array
	 * @return an array intersection of the first two
	 */
	public static int[] intersection(int[] arr1, int[] arr2) {
		return  IntStream.of(arr1).filter(y -> IntStream.of(arr2).anyMatch(x -> x == y)).toArray();
	}

	/**
	 * Find the sorted intersection of two sorted integer arrays.
	 *
	 * @param arr1 the first sorted array
	 * @param arr2 the second sorted array
	 * @return a sorted array intersection of the first two
	 */
	public static int[] intersectionSorted(int[] arr1, int[] arr2) {
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
	 * Union of 2 unsorted arrays without repetition
	 * @param arr1
	 * @param arr2
	 * @return
	 */

	public static int[] unionSet(int[] arr1, int[] arr2){
		return  Ints.toArray(ImmutableSet.copyOf(
				Ints.asList(Ints.concat(arr1,arr2))
		));
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
	 *
	 * @param needle   what to search
	 * @param haystack where to search
	 * @return true if the array contains the element, otherwise false
	 */
	public static boolean contains(int needle, int[] haystack) {
		// TODO: use binary search?
		for (int hay : haystack) {
			if (hay == needle)
				return true;
		}
		return false;
	}

	/**
	 * Search one element inside an array and returns the index of its position.
	 *
	 * @param needle   what to search
	 * @param haystack where to search
	 * @return the index in the array of the element, or -1
	 */
	public static int indexOf(int needle, int[] haystack) {
		// TODO: use binary search?
		for (int i = 0; i < haystack.length; i++) {
			if (haystack[i] == needle)
				return i;
		}
		return -1;
	}

	/**
	 * Returns the dimensions of 2d matrix
	 * @param matrix
	 * @return - tuple of integers
	 */

	public static int[] getShape(double[][] matrix){
		if(Arrays.stream(matrix).map(v -> v.length).distinct().count() != 1)
			throw new IllegalArgumentException("ERROR: nested vectors do not have the same length");
		return new int[] {matrix.length, matrix[0].length};
	}

	/**
	 * Returns the dimensions of 2d matrix
	 * @param matrix
	 * @return - tuple of integers
	 */

	public static int[] getShape(int[][] matrix){
		if(Arrays.stream(matrix).map(v -> v.length).distinct().count() != 1)
			throw new IllegalArgumentException("ERROR: nested vectors do not have the same length");
		return new int[] {matrix.length, matrix[0].length};
	}

	/**
	 * Transpose of a 2d matrix
	 * @param original
	 * @return
	 */
	public static double[][] transpose(double[][] original){
		int[] shape = getShape(original);
		double[][] transposed = new double[shape[1]][shape[0]];
		for(int i=0; i<shape[0]; i++)
			for(int j=0; j<shape[1]; j++)
				transposed[j][i] = original[i][j];

		return  transposed;

	}

	/**
	 * Transpose of a 2d matrix
	 * @param original
	 * @return
	 */
	public static int[][] transpose(int[][] original){
		int[] shape = getShape(original);
		int[][] transposed = new int[shape[1]][shape[0]];
		for(int i=0; i<shape[0]; i++)
			for(int j=0; j<shape[1]; j++)
				transposed[j][i] = original[i][j];

		return transposed;

	}

	/**
	 * Transforms a 1d vector into a 2d matrix
	 * @param vector - vector to transform
	 * @param shape - value of the 1st dimension or tuple with both of them.
	 * @return
	 */
	public static double[][] reshape2d(double[] vector, int... shape) {

		if(shape.length==1)
			shape = new int[]{shape[0], vector.length/shape[0]};

		if(shape[0]*shape[1] != vector.length)
			throw new IllegalArgumentException("ERROR: incompatible shapes");


		List data = Doubles.asList(vector);
		double[][] out = new double[shape[0]][shape[1]];

		for(int i=0; i<shape[0]; i++){
			double[] v = Doubles.toArray(data.subList(i*shape[1], (i+1)*shape[1]));
			out[i] = v;
		}

		return out;

	}
	/**
	 * Transforms a 1d vector into a 2d matrix
	 * @param vector - vector to transform
	 * @param shape - fully-defined vector specifying the 3 new dimensions.
	 * @return
	 */

	public static double[][][] reshape3d(double[] vector, int[] shape) {
        if(shape[0]*shape[1]*shape[2] != vector.length)
			throw new IllegalArgumentException("ERROR: incompatible shapes");

		double[][][] out = new double[shape[0]][][];
		int step = shape[1]*shape[2];
			for(int k=0; k<out.length;k++){
			out[k] = reshape2d(Arrays.copyOfRange(vector, k*step, (k+1)*step), new int[]{shape[1],shape[2]});
		}
		return out;
	}

	/**
	 * Given a vector representing a multidimensional array, swaps the axis.
	 * @param data	- matrix to transform
	 * @param sizes	- lengths of each axis
	 * @param newOrder - list of old axis in the new order.
	 * @return
	 */
	public static double[] swapVectorStrides(double[] data, int[] sizes, int[] newOrder ){

		int[] oldOrder = IntStream.range(0,sizes.length).toArray();
		int[] newSizes = IntStream.range(0,sizes.length).map(i -> sizes[newOrder[i]]).toArray();

		Strides oldStrides = new Strides(oldOrder, sizes);
		Strides newStrides = new Strides(newOrder, newSizes);

		double[] newdata = new double[oldStrides.getCombinations()];


		for(int offset=0; offset<newdata.length; offset++){
			int[] oldStates = oldStrides.statesOf(offset);
			int[] newStates = IntStream.range(0,sizes.length).map(i -> oldStates[newOrder[i]]).toArray();
			int newOffset = newStrides.getOffset(newStates);
			newdata[newOffset] = data[offset];
		}

		return newdata;
	}

	/**
	 *	Given a 1d vector of doubles, transforms it in a list of
	 *  tuples where the first element is the sequence number.
	 * @param vect
	 * @param start
	 * @return
	 */

	public static double[][] enumerate(double[] vect, int start){
		return IntStream.range(0+start, vect.length+start)
				.mapToObj(i -> new double[]{i, vect[i-start]})
				.toArray(double[][]:: new);
	}

	/**
	 *	Given a 1d vector of doubles, transforms it in a list of
	 *  tuples where the first element is the sequence number.
	 * @param vect
	 * @return
	 */
	public static double[][] enumerate(double[] vect){ return enumerate(vect,0);}

	/**
	 *	Generates a new vector without repeated values
	 * @param arr
	 * @return
	 */
	public static int[] unique(int[] arr){
		return  Ints.toArray(ImmutableSet.copyOf(
				Ints.asList(arr)
		));
	}


	/**
	 *	Generates a new vector without repeated values
	 * @param arr
	 * @return
	 */
	public static double[] unique(double[] arr){
		return  Doubles.toArray(ImmutableSet.copyOf(
				Doubles.asList(arr)
		));
	}

	/**
	 * Round all the values in a vector with a number of decimals.
	 * @param arr
	 * @param num_decimals
	 * @return
	 */
	public static double[] round(double arr[], int num_decimals){

		double[] data = Arrays.copyOf(arr, arr.length);

		for(int i=0; i<data.length; i++){
			data[i] = Precision.round(data[i], num_decimals);
		}

		return data;
	}

	/**
	 * Round non zero values such as the sum is equal to the target.
	 * @param arr
	 * @param target
	 * @param num_decimals
	 * @return
	 */
	public static double[] roundNonZerosToTarget(double[] arr, double target, int num_decimals){

		double[] data = Arrays.copyOf(arr, arr.length);

		data = ArraysUtil.round(data,3);
		BigDecimal sum = BigDecimal.valueOf(0.0);
		for(int i=0; i<data.length; i++){
			sum = sum.add(BigDecimal.valueOf(data[i]));
		}
		for(int i=data.length-1; i>=0; i--){
			if(data[i]!=0){
				sum = sum.subtract(BigDecimal.valueOf(data[i]));
				data[i] = BigDecimal.valueOf(target).subtract(sum).doubleValue();
				break;
			}
		}
		return data;
	}

	/**
	 * Generates the latex source for representing a 2d array.
	 * @param matrix
	 * @return
	 */
	public static String toLatex(double[][] matrix){
		String str = "";
		for(int i=0; i<matrix.length; i++){
			for(int j=0; j<matrix[i].length; j++){
				str+=matrix[i][j];
				if(j==matrix[i].length-1)
					str+=" \\\\\n";
				else
					str+=" &";
			}
		}
		return str;
	}

	/**
	 * Given a latex code representing a 1d vector, this function obtains
	 * the equivalent array of doubles.
	 * @param latexcode
	 * @return
	 */
	public static double[] latexToDoubleVector(String latexcode) {
		return  Stream.of(latexcode.split("&")).mapToDouble(s -> Double.valueOf(s)).toArray();
	}

	/**
	 * Given a latex code representing a 2d vector, this function obtains
	 * the equivalent array of doubles.
	 * @param latexcode
	 * @return
	 */
	public static double[][] latexToDoubleArray(String latexcode) {
		return  Stream.of(latexcode.split("\\\\"))
				.map(s -> latexToDoubleVector(s))
				.toArray(double[][]::new);
	}

	/**
	 * Given a latex code representing a 1d vector, this function obtains
	 * the equivalent array of ints.
	 * @param latexcode
	 * @return
	 */
	public static int[] latexToIntVector(String latexcode) {
		return  toIntVector(latexToDoubleVector(latexcode));
	}

	/**
	 * Given a latex code representing a 2d vector, this function obtains
	 * the equivalent array of ints.
	 * @param latexcode
	 * @return
	 */
	public static int[][] latexToIntArray(String latexcode) {
		return  toIntArray(latexToDoubleArray(latexcode));
	}

	/**
	 * cast a 1d array of doubles into one of ints
	 * @param arr
	 * @return
	 */
	public static int[] toIntVector(double[] arr){
		return Arrays.stream(arr).mapToInt(v -> (int)v).toArray();
	}


	/**
	 * cast a 2d array of doubles into one of ints
	 * @param arr
	 * @return
	 */
	public static int[][] toIntArray(double[][] arr){
		return Stream.of(arr).map(v -> toIntVector(v)).toArray(int[][]::new);
	}


	public static double[][] copyOfRange(double[][] original, int from, int to, int axis){

		double sliced[][] = null;

		if(axis==0){
			sliced = Arrays.copyOfRange(original, from, to);
		} else if (axis==1){
			sliced = new double[original.length][];
			for(int i =0; i<original.length; i++){
				sliced[i] = Arrays.copyOfRange(original[i], from, to);
			}
		}else{
			throw new IllegalArgumentException("axis cannot be greater than 1");
		}

		return sliced;

	}

	public static double[][] filterRows(double[][] array, Predicate<double[]> cond){
		//Predicate cond = v-> DoubleStream.of((double[]) v).anyMatch(x->x!=0);
		return IntStream.range(0,array.length).mapToObj(i -> array[i]).filter(cond).toArray(double[][]::new);
	}

	public static double[][] filterNonZeroRows(double[][] array){
		Predicate<double[]> cond = v-> DoubleStream.of(v).anyMatch(x->x!=0);
		return filterRows(array, cond);
	}

	public static int[] rowsWhere(double[][] array, Predicate<double[]> cond){
		return IntStream.range(0,array.length).filter(i->cond.test(array[i])).toArray();
	}
	public static int[] rowsWhereAllZeros(double[][] array){
		Predicate cond = v-> DoubleStream.of((double[]) v).allMatch(x->x==0);
		return rowsWhere(array, cond);
	}

	public static double[][] sliceRows(double[][] array, int... idx){
		return IntStream.of(idx).mapToObj(i->array[i]).toArray(double[][]::new);
	}

	public static double[][] sliceColumns(double[][] array, int... idx){
		double[][] out = new double[array.length][idx.length];
		for(int i=0;i<array.length;i++){
			for(int j=0; j<idx.length; j++){
				out[i][j] = array[i][idx[j]];
			}
		}
		return out;
	}

	public static double[][] dropRows(double[][] array, int... idx){
		int[] idx_comp = IntStream.range(0,ArraysUtil.getShape(array)[0]).filter(i -> !ArraysUtil.contains(i,idx)).toArray();
		return sliceRows(array,idx_comp);
	}

	public static double[][] dropColumns(double[][] array, int... idx){
		int[] idx_comp = IntStream.range(0,ArraysUtil.getShape(array)[1]).filter(i -> !ArraysUtil.contains(i,idx)).toArray();
		return sliceColumns(array,idx_comp);
	}



	public static int ndim(Object array){
		return ndimWithClass(array.getClass());
	}

	private static int ndimWithClass(Class array){
		if(!array.getComponentType().getName().startsWith("["))
			return 1;
		return ndimWithClass(array.getComponentType())+1;
	}

	/**
	 * Flatten a list containing a n-dimensional array of doubles
	 * @param a
	 * @return
	 */
	public static double[] flattenDoubles(List a)
	{
		int ndims = ArraysUtil.ndim(a.get(0));
		if(ndims >1){

			ListIterator it =  a.listIterator();
			List aux = new ArrayList();

			while (it.hasNext()) {
				List sublist;
				if (ndims >= 3) {
					sublist = List.of((Object[][]) it.next());
				} else {
					sublist = List.of((double[][]) it.next());
				}
				aux.add(flattenDoubles(sublist));
			}

			a = aux;

		}
		double[] out = ((double[])a.get(0));
		for(int i=1; i<a.size(); i++)
			out = Doubles.concat(out, ((double[])a.get(i)));
		return out;

	}

	/**
	 * Flatten a list containing a n-dimensional array of integers
	 * @param a
	 * @return
	 */
	public static int[] flattenInts(List a)
	{
		int ndims = ArraysUtil.ndim(a.get(0));
		if(ndims >1){

			ListIterator it =  a.listIterator();
			List aux = new ArrayList();

			while (it.hasNext()) {
				List sublist;
				if (ndims >= 3) {
					sublist = List.of((Object[][]) it.next());
				} else {
					sublist = List.of((int[][]) it.next());
				}
				aux.add(flattenInts(sublist));
			}

			a = aux;

		}
		int[] out = ((int[])a.get(0));
		for(int i=1; i<a.size(); i++)
			out = Ints.concat(out, ((int[])a.get(i)));
		return out;

	}
	public static void main(String[] args) {
		int[][][][] v = {{{{1, 2}, {3, 4}}}, {{{9, 2}, {8, 4}}}};
		System.out.println(Arrays.toString(flattenInts(List.of(v)))) ;
	}


}

