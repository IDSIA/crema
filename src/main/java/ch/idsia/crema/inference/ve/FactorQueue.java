package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.GenericFactor;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;

public class FactorQueue<F extends GenericFactor> implements Iterator<ArrayList<F>> {

	private final TIntObjectHashMap<ArrayList<F>> data;
	private int[] sequence;

	public FactorQueue(int[] sequence) {
		this.data = new TIntObjectHashMap<>();
		this.sequence = sequence;
		for (int var : sequence) {
			this.data.put(var, new ArrayList<>());
		}
	}

	public void add(F factor) {
		for (int variable : sequence) {
			if (factor.getDomain().contains(variable)) {
				data.get(variable).add(factor);
				return;
			}
		}

		// do not include the factor as it is not covered by the remaining variables in the sequence
	}

	public void addAll(List<F> factors) {
		LinkedList<F> items = new LinkedList<>(factors);
		for (int variable : sequence) {
			ListIterator<F> iterator = items.listIterator();
			while (iterator.hasNext()) {
				F f = iterator.next();
				if (f.getDomain().contains(variable)) {
					data.get(variable).add(f);
					iterator.remove();
				}
			}
		}
	}

	public int getVariable() {
		return sequence[0];
	}

	@Override
	public boolean hasNext() {
		return sequence.length > 0;
	}

	@Override
	public ArrayList<F> next() {
		int next = sequence[0];
		ArrayList<F> factors = data.get(next);
		int[] new_sequence = new int[sequence.length - 1];
		System.arraycopy(sequence, 1, new_sequence, 0, new_sequence.length);
		sequence = new_sequence;
		data.remove(next);
		return factors;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
