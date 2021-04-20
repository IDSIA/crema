package ch.idsia.crema.factor.operations.vertex;

import org.apache.commons.math3.util.FastMath;

public class Util {

	public final double logSum(double first, double second) {
		final double min, max;
		if (first < second) {
			min = first;
			max = second;
		} else {
			min = second;
			max = first;
		}

		return max + FastMath.log(FastMath.exp(min - max) + 1);
	}
}
