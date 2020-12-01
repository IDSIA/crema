package ch.idsia.crema.utility;

import ch.idsia.crema.core.Strides;
import gnu.trove.iterator.TIntIntIterator;

/**
 * Iterator over two separate strides/
 *
 * @author david
 */
public class SeparateIndexIterator implements TIntIntIterator {

	private final IndexIterator conditioningIterator;
	private final IndexIterator dataIterator;
	private int key;
	private int value;

	/**
	 * Iterate the two domains
	 *
	 * @param data
	 * @param conditioning
	 */
	public SeparateIndexIterator(Strides data, Strides conditioning) {
		Strides union = data.union(conditioning);
		this.dataIterator = data.getIterator(union);
		this.conditioningIterator = conditioning.getIterator(union);
		this.key = -1;
		this.value = -1;
	}

	public SeparateIndexIterator(IndexIterator data, IndexIterator conditioning) {
		this.dataIterator = data;
		this.conditioningIterator = conditioning;
		this.key = -1;
		this.value = -1;
	}

	@Override
	public void advance() {
		key = conditioningIterator.next();
		value = dataIterator.next();
	}

	@Override
	public boolean hasNext() {
		dataIterator.hasNext();
		return conditioningIterator.hasNext();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove from this iterator");
	}

	public int getConditioningIndex() {
		return key();
	}

	public int getDataIndex() {
		return value();
	}

	@Override
	public int key() {
		return key;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public int setValue(int val) {
		return value;
	}

}
