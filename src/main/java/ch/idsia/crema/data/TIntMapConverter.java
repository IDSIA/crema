package ch.idsia.crema.data;

import java.util.Arrays;

import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class TIntMapConverter {

	/**
	 * Convert from the specified map to an array of int using the specified columns
	 * order
	 * 
	 * @param map     the map to be converted
	 * @param columns the order of the columns
	 * @return the converted integer array
	 */
	public static int[] from(Int2IntMap map, int[] columns) {
		return Arrays.stream(columns).map(map::get).toArray();
	}

	/**
	 * A curried version of the from method that returns a version of from with
	 * fixed columns
	 * 
	 * @param columns the order to be fixed
	 * @return the function
	 */
	public static Function<Int2IntMap, int[]> curriedFrom(int[] columns) {
		return map -> Arrays.stream(columns).map(map::get).toArray();
	}

	/**
	 * Convert an array of maps to an array of int arrays sorted by columns.
	 * 
	 * @param map     the input data
	 * @param columns the order of the columns
	 * 
	 * @return the output data
	 */
	public static int[][] from(Int2IntMap[] map, int[] columns) {
		return Arrays.stream(map).map(curriedFrom(columns)).toArray(int[][]::new);
	}

	/**
	 * Discover the set of columns assuming that not all are specified
	 * 
	 * @param data
	 * @return
	 */
	protected static int[] cols(Int2IntMap[] data) {
		IntSet columns = new IntOpenHashSet();
		for (Int2IntMap row : data) {
			columns.addAll(row.keySet());
		}
		return columns.toIntArray();
	}	
}