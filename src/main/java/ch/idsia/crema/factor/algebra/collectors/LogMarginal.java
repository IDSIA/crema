package ch.idsia.crema.factor.algebra.collectors;

import org.apache.commons.math3.util.FastMath;

public final class LogMarginal implements Collector {

	private final int[] offsets;
	private final int size;

	@Override
	public final double collect(double[] data, int source) {
		double value = data[source + offsets[0]];
		for (int i = 0; i < size; ++i) {
			double v = data[source + offsets[i]]; 
			if (v > value) {
				value = v + Math.log1p(FastMath.exp(value - v));
			} else {
				value += Math.log1p(FastMath.exp(v - value));
			}
		}
		return value;
	}

	public LogMarginal(int size, int stride) {
		this.size = size;
		offsets = new int[size];
		for (int i = 0; i < size; ++i) {
			offsets[i] = i * stride;
		}
	}
}