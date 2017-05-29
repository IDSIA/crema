package ch.idsia.crema.utility;

import java.util.*;

/**
 * Iterator over the possible combinations of the elements of the arrays given at construction time.
 * The input arrays are given as an array of collections or a collection of collections. 
 * The order of the elements is kept the on of the input.
 * <p>Example:
 * <pre>
 * Collection&lt;Integer&gt; items1 = Arrays.asList(2,4,6);
 * Collection&lt;String&gt; items2 = Arrays.asList("1","3");
 * Iterator<Collection<Object>> iter = new PermutationIterator(item1, item2);
 * </pre>
 * The iterator will return in sequence collections containing:
 * <pre>
 * 2, "1"
 * 2, "3"
 * 4, "1"
 * 4, "3"
 * 6, "1"
 * 6, "3"
 * </pre> 
 * @author david
 *
 */
public class IntCombinationsIterator implements Iterator<int[]> {

	private int total;
	private int counter;

	private int[] sizes;
	private int[] current;

	public IntCombinationsIterator(int[] source) {
		this.sizes = source.clone();
		this.current = new int[source.length];

		this.counter = -1;
		this.total = 1;
		for (int sz : source) {
			total *= sz;
		}
	}

	public int size() {
		return total;
	}

	@Override
	public boolean hasNext() {
		return counter < total-1;
	}

	@Override
	public int[] next() {
		++counter; // counter starts from -1!
		int index = counter;
		for (int sz_index = 0; sz_index < sizes.length; sz_index ++) {
			int sz = sizes[sz_index];
			current[sz_index] = index % sz;
			index /= sz;
		}
		return current.clone();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove is not implemented");
	}
}
