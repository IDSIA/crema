package ch.idsia.crema.utility;

import ch.idsia.crema.core.Strides;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;


/**
 * Iterator over two separate strides/
 *
 * @author david
 */
public class SeparateIndexIterator implements ObjectIterator<Entry> {

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
	public boolean hasNext() {
		dataIterator.hasNext();
		return conditioningIterator.hasNext();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove from this iterator");
	}

	public int getConditioningIndex() {
		return key;
	}

	public int getDataIndex() {
		return value;
	}


	@Override
	public Entry next() {
		key = conditioningIterator.nextInt();
		value = dataIterator.nextInt();
		
		return new Entry() {
			int value = SeparateIndexIterator.this.value;
			int key = SeparateIndexIterator.this.key;
			
			@Override
			public int setValue(int value) {
				return 0;
			}
			
			@Override
			public int getIntValue() {
				return value;
			}
			
			@Override
			public int getIntKey() {
				return key;
			}
		};
	}

}
