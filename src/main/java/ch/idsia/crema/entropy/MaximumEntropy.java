package ch.idsia.crema.entropy;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.ArrayUtils;

public class MaximumEntropy {

	public static final MaximumEntropy INSTANCE = new MaximumEntropy();

	public double[] compute(double[] lowers, double[] uppers) {
		int size = lowers.length;
		double[] lower_copy = ArrayUtils.clone(lowers);

		TIntSet indices = new TIntHashSet();
		for (int i = 0; i < size; ++i) indices.add(i);

		maxEntropy(lower_copy, uppers, indices);
		return lower_copy;
	}

	private void maxEntropy(double[] l, double[] u, TIntSet S) {
		int len = l.length;

		if (sum(l) < 1) {
			for (int i = 0; i < len; ++i) {
				if (Math.abs(u[i] - l[i]) < 0.000000000001) {
					S.remove(i);
				}
			}

			double s = sum(l);

			TIntIterator S_iter = S.iterator();
			int r = min(l, S_iter);
			int f = sig(l, S_iter);
			int m = nmin(l, S_iter);

			for (int i = 0; i < len; ++i) {
				if (l[i] == l[min(l, S_iter)]) {
					double ll = Math.min(u[i] - l[i], (1 - s) / m);
					if (sig(l, S_iter) == -1) {
						ll = Math.min(ll, 1);
					} else if (f >= 0) {
						ll = Math.min(ll, l[f] - l[r]);
					}
					l[i] += ll;
				}
			}

			maxEntropy(l, u, S);
		}
	}

	private int sig(double[] l, TIntIterator S) {
		double min = 1;
		int mig = -1;
		double smin = 1;
		int sig = -1;

		while (S.hasNext()) {
			int i = S.next();
			if (min > l[i]) {
				if (mig >= 0) {
					sig = mig;
					smin = min;
				}
				min = l[i];
				mig = i;
			} else if (l[i] != min && smin > l[i]) {
				sig = i;
				smin = l[i];
			}
		}
		return sig;
	}

	private int nmin(double[] l, TIntIterator S) {
		double min = 1;
		int nmin = 0;

		while (S.hasNext()) {
			int i = S.next();
			if (min > l[i]) {
				min = l[i];
				nmin = 1;
			} else if (min == l[i]) {
				++nmin;
			}
		}
		return nmin;
	}

	private int min(double[] l, TIntIterator S) {
		double min = 1;
		int mig = -1;

		while (S.hasNext()) {
			int i = S.next();
			if (min > l[i]) {
				min = l[i];
				mig = i;
			}
		}
		return mig;
	}

	private double sum(double[] v) {
		double val = 0;
		for (double item : v) val += item;
		return val;
	}
}