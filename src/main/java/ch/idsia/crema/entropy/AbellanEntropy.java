package ch.idsia.crema.entropy;

import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.DoubleStream;

public class AbellanEntropy {

	/**
	 * Return the index of the minimum value of arr among the values such that the corresponding elements of b are true.
	 *
	 * @param arr
	 * @param b
	 * @return
	 */
	private int minLS(double[] arr, boolean[] b) {
		int index = 0;
		boolean[] b2 = ArrayUtils.clone(b);
		double myMin;
		if (!allFalse(b2)) {
			for (int i = 0; i < arr.length; i++) {
				if (b2[i]) {
					index = i;
					break;
				}
			}
			myMin = arr[index];
			for (int i = 0; i < b2.length; i++) {
				if (b2[i]) {
					if (arr[i] < myMin) {
						myMin = arr[i];
						index = i;
					}
				}
			}
		} else {
			index = -1;
		}
		return index;
	}

	/**
	 * Return the number of occurrences of the minimum of arr only over the values of arr such that the corresponding
	 * element of b is true.
	 *
	 * @param arr
	 * @param b
	 * @return
	 */
	private int nMinLS(double[] arr, boolean[] b) {
		double myMin = arr[minLS(arr, b)];
		int q = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i]) {
				if (Math.abs(arr[i] - myMin) < 1E-10) {
					q++;
				}
			}
		}
		return q;
	}

	/**
	 * Find the index of the second smallest element of arr among the values corresponding to the true values of b.
	 *
	 * @param arr
	 * @param b
	 * @return
	 */
	private int secondMinLS(double[] arr, boolean[] b) {
		boolean[] b2 = ArrayUtils.clone(b);
		int index = minLS(arr, b2);
		// FIXME: what if index is -1?
		double min1 = arr[index];
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == min1)
				b2[i] = false;
		int index2 = -1;
		//if(index!=-1){ b[index]=false;
		index2 = minLS(arr, b2);//}
		return index2;
	}

	/**
	 * Boolean function to check whether or not all the elements of arr are false.
	 *
	 * @param arr
	 * @return
	 */
	private boolean allFalse(boolean[] arr) {
		for (boolean b : arr)
			if (b)
				return false;
		return true;
	}

	/**
	 * @param l
	 * @param u
	 * @return
	 */
	public double[] getMaxEntropy(double[] l, double[] u) {
		// ALGORITHM
		double ss;
		int r, f, m;
		boolean[] S = new boolean[l.length];

		for (int i = 0; i < l.length; i++) {
			S[i] = true;
		}

		// S initialisation
		while (DoubleStream.of(l).sum() < 1.0) {
			for (int i = 0; i < l.length; i++) {
				if (u[i] == l[i]) {
					S[i] = false;
				}
			}
			ss = DoubleStream.of(l).sum();
			r = minLS(l, S);
			f = secondMinLS(l, S);
			m = nMinLS(l, S);
			for (int i = 0; i < l.length; i++) {
				if (l[i] == l[minLS(l, S)]) {
					if (f == -1) {
						l[i] += Math.min(u[i] - l[i], Math.min((1 - ss) / m, 1));
					} else {
						l[i] += Math.min(u[i] - l[i], Math.min(l[f] - l[r], (1 - ss) / m));
					}
				}
			}
		}
		return l;
	}
}