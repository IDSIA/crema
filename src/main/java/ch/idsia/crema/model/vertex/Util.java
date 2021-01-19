package ch.idsia.crema.model.vertex;

import org.apache.commons.math3.util.FastMath;

public class Util {

	public final double logsum(double one, double second) {
		final double min, max;
		if (one < second) {
			min = one;
			max = second;
		} else {
			min = second;
			max = one;
		}

		return max + FastMath.log(FastMath.exp(min - max) + 1);
	}
}
