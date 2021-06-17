package ch.idsia.crema.utility.hull;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static ch.idsia.crema.utility.ProbabilityUtil.infoLoss;

public class ReducedHull implements ConvexHull {

	private int numPoints = -1;

	private List<double[]> points;
	private double[][] dist;
	private double[][][] repl;

	private int dim;
	private int N;

	private boolean fixedNumPoints = false;

	public ReducedHull() {
	}

	public ReducedHull(int numPoints) {
		this.numPoints = numPoints;
		fixedNumPoints = true;
	}

	private void init(double[][] vertices) {
		// Get list structures: points, distances, midpoint

		this.dim = vertices[0].length;
		this.N = vertices.length;

		this.dist = new double[N][N];
		this.repl = new double[N][N][dim];
		this.points = new ArrayList<>(N);

		Collections.addAll(this.points, vertices);

		for (int i = 0; i < N - 1; i++) {
			for (int j = i; j < N; j++) {
				updateInfo(i, j);
			}
		}
	}

	private void updatePoint(int i, double[] point) {
		this.points.set(i, point);
	}

	private void updateInfo(int i) {
		for (int j = 0; j < N; j++)
			if (i != j)
				updateInfo(i, j);
	}

	private void updateInfo(int i, int j) {
		double[] q;
		double d;

		if (areValid(i, j)) {

			double[] pi = points.get(i);
			double[] pj = points.get(j);
			q = IntStream.range(0, pi.length).mapToDouble(k -> 0.5 * (pi[k] + pj[k])).toArray();
			d = infoLoss(pi, pj, q, true);
		} else {
			q = null;
			d = Double.NaN;
		}

		if (i < j) {
			dist[i][j] = d;
			repl[i][j] = q;
		} else {
			dist[j][i] = d;
			repl[j][i] = q;
		}
	}

	private double getDist(int i, int j) {
		if (i < j) return this.dist[i][j];
		return this.dist[j][i];
	}

	private double[] getRepl(int i, int j) {
		if (i < j) return this.repl[i][j];
		return this.repl[j][i];
	}

	private boolean areValid(int... idx) {
		for (int i : idx)
			if (this.points.get(i) == null)
				return false;
		return true;
	}

	private void removeNeighbour() {
		double minDist = Double.POSITIVE_INFINITY;

		int idx1 = -1;
		int idx2 = -1;

		// find the pair of point with the minimal loss
		for (int i = 0; i < N - 1; i++) {
			for (int j = i + 1; j < N; j++) {
				if (areValid(i, j)) {
					double d = getDist(i, j);
					if (minDist > d) {
						minDist = d;
						idx1 = i;
						idx2 = j;
					}
				}
			}
		}

		updatePoint(idx1, getRepl(idx1, idx2));
		updatePoint(idx2, null);

		updateInfo(idx1);
		updateInfo(idx2);
	}

	/**
	 * Set the number of points
	 *
	 * @param numPoints
	 * @return
	 */
	public ReducedHull setNumPoints(int numPoints) {
		if (fixedNumPoints)
			throw new IllegalStateException("Number of points cannot be changed when fixed in the constructor");
		this.numPoints = numPoints;
		return this;
	}

	/**
	 * Method that applies the given convex hull method to a list of vertices.
	 *
	 * @param vertices: 2D array of doubles where the first dimension is the number of points and the second
	 *                  is the dimensionality of the points.
	 * @return Array of vertices after applying the convex hull method.
	 */
	@Override
	public double[][] apply(double[][] vertices) {
		double[][] hull = ConvexHull.LP_CONVEX_HULL.apply(vertices);
		int m = numPoints;
		if (m < 0) m = vertices.length / 2;
		init(hull);
		for (int i = 0; i < (hull.length - m); i++)
			removeNeighbour();
		return this.points.stream().filter(Objects::nonNull).toArray(double[][]::new);
	}

}
