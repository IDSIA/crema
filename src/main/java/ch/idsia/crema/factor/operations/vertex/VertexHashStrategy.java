package ch.idsia.crema.factor.operations.vertex;

import gnu.trove.strategy.HashingStrategy;

public class VertexHashStrategy implements HashingStrategy<double[]> {
	private static final long serialVersionUID = -8829559491624233597L;

	private final double eps = 0.000000000001;

	@Override
	public int computeHashCode(double[] a) {
		if (a == null)
			return 0;

		int result = 1;
		for (double element : a) {
			long bits = Double.doubleToLongBits(round(element));
			result = 31 * result + (int) (bits ^ (bits >>> 32));
		}
		return result;
	}

	private double round(double value) {
		return ((long) (value / eps + 0.5)) * eps;
	}

	@Override
	public boolean equals(double[] a, double[] a2) {
		if (a == a2)
			return true;
		if (a == null || a2 == null)
			return false;

		int length = a.length;
		if (a2.length != length)
			return false;

		for (int i = 0; i < length; i++)
			if (Double.doubleToLongBits(round(a[i])) != Double.doubleToLongBits(round(a2[i])))
				return false;

		return true;
	}
}
