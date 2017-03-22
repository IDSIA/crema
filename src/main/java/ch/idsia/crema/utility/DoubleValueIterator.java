package ch.idsia.crema.utility;

public class DoubleValueIterator {
	private IndexIterator iterator;
	private double[] data;
	
	public DoubleValueIterator(IndexIterator iterator, double[] data) {
		this.iterator = iterator;
		this.data = data;
	}
	
	public boolean hasNext() {
		return iterator.hasNext();
	}
	
	public double next() {
		int i = iterator.next();
		return data[i];
	}
}
