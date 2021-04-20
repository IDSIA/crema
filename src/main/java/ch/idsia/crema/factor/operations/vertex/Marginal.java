package ch.idsia.crema.factor.operations.vertex;

public final class Marginal implements Collector {

	private final int[] offsets;
	private final int size;

	@Override
	public final double collect(final double[] data, final int source) {
		double value = 0;
		for (int i = 0; i < size; ++i) {
			value += data[source + offsets[i]];
		}
		return value;
	}

	public Marginal(int size, int stride) {
		this.size = size;
		offsets = new int[size];
		for (int i = 0; i < size; ++i) {
			offsets[i] = i * stride;
		}
	}
}