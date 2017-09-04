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
 * @param <E>
 */
public class CombinationsIterator<E> implements Iterator<Collection<E>> {

	private int total;
	private int counter;

	private ArrayList<ArrayList<E>> collections;
	private ArrayList<E> current;
	
	public CombinationsIterator(Collection<Collection<E>> source) {
		this.collections = new ArrayList<>(source.size());
		this.current = new ArrayList<>(source.size());

		this.counter = -1;
		this.total = 1;
		
		// make a copy of each collection
		for (Collection<E> collection : source) {
			ArrayList<E> copy = new ArrayList<>(collection);
			this.collections.add(copy);
			this.current.add(null);
			this.total *= copy.size(); 
		}
	}
	
	public int size() {
		return total;
	}
	public CombinationsIterator(@SuppressWarnings("unchecked") Collection<E>... collectionsList) {
		this(Arrays.asList(collectionsList));
	}
	
	@Override
	public boolean hasNext() {
		return counter < total-1;
	}

	@Override
	public List<E> next() {
		++counter;
		int index = counter;
		for (int collectionIndex = collections.size() - 1; collectionIndex >= 0; -- collectionIndex) {
			ArrayList<E> collection = collections.get(collectionIndex);
			int offset = index % collection.size();
			current.set(collectionIndex, collection.get(offset));
			index /= collection.size();
		}
		return Collections.unmodifiableList(current);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove is not implemented");
	}
}
