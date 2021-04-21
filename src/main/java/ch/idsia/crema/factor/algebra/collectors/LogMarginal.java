package ch.idsia.crema.factor.algebra.collectors;

import org.apache.commons.math3.util.FastMath;

public final class LogMarginal implements Collector {

	private final int[] offsets;
	private final int size;

	@Override
	public final double collect(final double[] data, final int source) {
		double value = 0;
		for (int i = 0; i < size; ++i) {
			value += FastMath.exp(data[source + offsets[i]]);
		}
		return FastMath.log(value);
	}

	public LogMarginal(int size, int stride) {
		this.size = size;
		offsets = new int[size];
		for (int i = 0; i < size; ++i) {
			offsets[i] = i * stride;
		}
	}
}