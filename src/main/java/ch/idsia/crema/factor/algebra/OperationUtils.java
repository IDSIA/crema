package ch.idsia.crema.factor.algebra;

import org.apache.commons.math3.util.FastMath;

public class OperationUtils {

	private OperationUtils() {
	}

	
	public static double logSum(double first, double second) {
		final double min, max;
		if (first < second) {
			min = first;
			max = second;
		} else {
			min = second;
			max = first;
		}

		return max + FastMath.log1p(FastMath.exp(min - max));
	}

}
