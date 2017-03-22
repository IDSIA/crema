package ch.idsia.crema.utility;

import ch.idsia.crema.model.Strides;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

public class IndexIterator implements TIntIterator {

	// "constant" parts
	final private int[] strides;
	final private int[] sizes;
	final private int[] reset;
	
	// not constant stuff (the array is final, but its content changes during iteration)
	final private int[] moving_positions;

	private int current_index = 0;
	private int steps = 0;

	/** 
	 * Create a new Index
	 * @param o
	 * @return
	 */
	public IndexIterator offset(int o) {
		return new IndexIterator(strides, sizes, reset, moving_positions.clone(), o, steps);
	}

	/**
	 * Iterated over the specified domain.
	 * 
	 * @param domain
	 */
	public IndexIterator(Strides domain) {
		this(domain.getStrides(), domain.getSizes(), 0, domain.getCombinations());
	}

	
	public IndexIterator(int[] strides, int[] sizes, int steps) {
		this(strides, sizes, 0, steps);
	}
	
	public IndexIterator(int[] strides, int[] sizes, int offset, int steps) {
		this.strides = strides;
		this.sizes = sizes;
		this.steps = steps;
		this.moving_positions = new int[sizes.length];
		this.current_index = offset;
		this.reset = new int[sizes.length];

		for (int i = 0; i < reset.length; ++i) {
			reset[i] = (sizes[i] - 1) * strides[i];
		}
	}
	
	

	public IndexIterator(int[] strides, int[] sizes, int[] reset, int[] moving_positions, int current_index, int steps) {
		super();
		this.strides = strides;
		this.sizes = sizes;
		this.reset = reset;
		this.moving_positions = moving_positions;
		this.current_index = current_index;
		this.steps = steps;
	}
	

	/**
	 * Make a copy of the iterator for separate use. The new iterator can safely be used independenlty. 
	 * Note that the memory usage is less than creating a new IndexIterator as the strides, sizes and reset values are not cloned. 
	 */
	@Override
	public IndexIterator clone() {
		// only the moving_positions array needs to be cloned! (the steps and the current_index are passed by value)
		return new IndexIterator(this.strides, this.sizes, this.reset, this.moving_positions.clone(), this.current_index, this.steps); 
	} 

	@Override
	public boolean hasNext() {
		return steps > 0;
	}

	@Override
	public int next() {
		final int result = current_index;

		for (int index = 0; index < sizes.length; ++index) {

			// move to the next step the first variable that did not already
			// reached the end
			if (++moving_positions[index] < sizes[index]) {
				current_index += strides[index];

				// we've got a valid index change
				break;

			} else {
				// variables that reached the end have to be rewind a little.
				current_index -= reset[index];
				moving_positions[index] = 0;

				// no break here! since we have to increase a variable
			}
		}

		// finally test if we reached the last index
		--steps;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Index iterator does not support removals");
	}
	
	public int[] getPositions() {
		return moving_positions;
	}

	public int[] toArray() {
		TIntArrayList result = new TIntArrayList();
		while (hasNext())
			result.add(next());
		return result.toArray();
	}
}