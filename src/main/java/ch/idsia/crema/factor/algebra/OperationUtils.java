package ch.idsia.crema.factor.algebra;

import org.apache.commons.math3.util.FastMath;

public class OperationUtils {

	private OperationUtils() {
	}

	
	public static double logSum(double first, double second) {
		double min;
		double max;

		if (first < second) {
			min = first;
			max = second;
		} else {
			min = second;
			max = first;
		}

		return max + Math.log1p(Math.exp(min - max));
	}
	
	public static double logSumLowPrecision(double first, double second) {
		return second + FastMath.log1p(FastMath.exp(first - second));
	}
}
