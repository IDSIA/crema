package ch.idsia.crema.model.vertex;

import org.apache.commons.math3.util.FastMath;

public class SimpleVertexOperation implements VertexOperation {

	@Override
	public double[] combine(double[] t1, double[] t2, int size, long[] stride, long[] reset, int[] limits) {
		int length = limits.length;

		double[] result = new double[size];
		double[] assign = new double[length];

		long idx = 0;
		for (int i = 0; i < size; ++i) {
			result[i] = t1[(int) idx] * t2[(int) (idx >>> 32L)];

			for (int l = 0; l < length; ++l) {
				if (assign[l] == limits[l]) {
					assign[l] = 0;
					idx -= reset[l];
				} else {
					++assign[l];
					idx += stride[l];
					break;
				}
			}
		}

		return result;
	}

	@Override
	public double[] marginalize(double[] data, int size, int stride) {
		int reset = size * stride;
		int target_size = data.length / size;
		int source = 0;
		int next = stride;
		int jump = reset - stride;

		final double[] result = new double[target_size];

		for (int target = 0; target < target_size; ++target, ++source) {
			if (source == next) {
				source += jump;
				next += reset;
			}

			double value = data[source];
			for (int idx = source + stride; idx < reset; ++idx) {
				value += data[idx];
			}

			result[target] = FastMath.log(value);
		}

		return result;
	}

	@Override
	public double convert(double val) {
		return val;
	}

	@Override
	public double[] convert(double[] data) {
		return data;
	}

	@Override
	public double revert(double val) {
		return val;
	}

	@Override
	public double[] revert(double[] data) {
		return data;
	}
}
