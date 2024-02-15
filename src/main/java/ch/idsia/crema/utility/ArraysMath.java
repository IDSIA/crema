package ch.idsia.crema.utility;

import org.apache.commons.math3.util.FastMath;

public class ArraysMath {
	
	public static double sum(double[] values) {
		double total = 0;
		for (int i = 0; i < values.length; ++i) {
			total += values[i];
		}
		return total;
	}
	
	public static int sum(int[] values) {
		int total = 0;
		for (int i = 0; i < values.length; ++i) {
			total += values[i];
		}
		return total;
	}

	public static long sum(long[] values) {
		long total = 0;
		for (int i = 0; i < values.length; ++i) {
			total += values[i];
		}
		return total;
	}
	
	
	public static int max(int[] array) {
		int m = Integer.MIN_VALUE;
		for (int i = 0; i < array.length;++i) {
			m = Math.max(m, array[i]);
		}
		return m;
	}
	
	public static int min(int[] array) {
		int m = Integer.MAX_VALUE;
		for (int i = 0; i < array.length;++i) {
			m = Math.min(m, array[i]);
		}
		return m;
	}
	
	public static double max(double[] array) {
		double m = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < array.length;++i) {
			m = Math.max(m, array[i]);
		}
		return m;
	}
	
	public static double min(double[] array) {
		double m = Double.POSITIVE_INFINITY;
		for (int i = 0; i < array.length;++i) {
			m = Math.min(m, array[i]);
		}
		return m;
	}
	
	
	public static double mean(int[] array) {
		double m = 0;
		// consider using https://en.wikipedia.org/wiki/Kahan_summation_algorithm
		for (int i = 0; i < array.length;++i) {
			m += array[i];
		}
		return m / (double)array.length;
	}
	

	
	public static double mean(long[] array) {
		double m = 0;
		// consider using https://en.wikipedia.org/wiki/Kahan_summation_algorithm
		for (int i = 0; i < array.length;++i) {
			m += array[i];
		}
		return m / (double)array.length;
	}
	
	
	public static double mean(double[] array) {
		double m = 0;
		// consider using https://en.wikipedia.org/wiki/Kahan_summation_algorithm
		for (int i = 0; i < array.length;++i) {
			m += array[i];
		}
		return m / (double)array.length;
	}

	
	public static double sd(int[] array, double ddof) {
		double m = mean(array);
		double s = 0;
		for (int i = 0; i < array.length;++i) {
			double d = (double)array[i] - m;
			s += d * d;
		}
		return FastMath.sqrt(s / ((double)array.length - ddof));
	}
	

	
	public static double sd(long[] array, double ddof) {
		double m = mean(array);
		double s = 0;
		for (int i = 0; i < array.length;++i) {
			double d = (double)array[i] - m;
			s += d * d;
		}
		return FastMath.sqrt(s / ((double)array.length - ddof));
	}
	
	public static double sd(double[] array, double ddof) {
		double m = mean(array);
		double s = 0;
		for (int i = 0; i < array.length;++i) {
			double d = array[i] - m;
			s += d * d;
		}
		return FastMath.sqrt(s / ((double)array.length - ddof));
	}
	
}
