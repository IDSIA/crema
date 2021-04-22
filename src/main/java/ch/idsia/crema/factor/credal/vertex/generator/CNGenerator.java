package ch.idsia.crema.factor.credal.vertex.generator;

import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.RandomUtil;
import ch.javasoft.polco.adapter.Options;
import ch.javasoft.polco.adapter.PolcoAdapter;
import ch.javasoft.xml.config.XmlConfigException;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;

// TODO: this class should NOT be in a factor package
public class CNGenerator {
	static Random random = RandomUtil.getRandom();

	public static double[] randomMassFunction(int dimension) {
		double[] p = new double[dimension];
		double sum = 0.0;
		for (int j = 0; j < dimension; j++) {
			p[j] = random.nextDouble();
			sum += p[j];
		}
		for (int j = 0; j < dimension; j++) {
			p[j] /= sum;
		}
		return p;
	}

	public double[][] linvac(int dimension, double epsilon) {
		double sum;
		double[][] v2 = new double[dimension][dimension]; // first d is # vertices
		double[] p = randomMassFunction(dimension);
		for (int i = 0; i < dimension; i++) {
			sum = 0.0;
			for (int j = 0; j < (dimension - 1); j++) {
				if (j == i) {
					v2[i][j] = epsilon + (1 - epsilon) * p[j];
				} else {
					v2[i][j] = (1 - epsilon) * p[j];
				}
				v2[i][j] = Math.round(v2[i][j] * 100000d) / 100000d;
				sum += v2[i][j];
			}
			v2[i][dimension - 1] = Math.round((1.0 - sum) * 100000d) / 100000d;
		}
		return v2;
	}

	/**
	 * Convert probability intervals into reachable ones
	 *
	 * @param bounds
	 * @return
	 */
	public double[][] makeReachable(double[][] bounds) {
		// TODO: DEBUG: add non-emptiness check
		int d = bounds[0].length;
		double[] reachValue = new double[2];
		for (int i = 0; i < d; i++) {
			reachValue[0] = 1.0;
			reachValue[1] = 1.0;
			for (int j = 0; j < d; j++) {
				if (i != j) {
					reachValue[0] -= bounds[1][j];
					reachValue[1] -= bounds[0][j];
				}
			}
			if (reachValue[0] > bounds[0][i]) {
				bounds[0][i] = reachValue[0];
			}
			if (reachValue[1] < bounds[1][i]) {
				bounds[1][i] = reachValue[1];
			}
		}
		return bounds;
	}

	/**
	 * Compute the extreme points of a probability interval using the whole dimensionality.
	 *
	 * @param i a probability interval
	 * @return
	 */
	public double[][] fromInt2VertFullD(double[][] i) {
		// Using polco to compute vertices
		PolcoAdapter p;
		Options opts = new Options();
		opts.setLoglevel(Level.WARNING);
		try {
			p = new PolcoAdapter(opts);
		} catch (XmlConfigException e) {
			e.printStackTrace();
			throw new RuntimeException("An error occured while initializing polco", e);
		}

		int d = i[0].length;
		// Set linear constraints related to the d states
		// and an equality constraints (normalization)
		double[][] ineqs = new double[2 * d][d + 1];
		double[][] eqs = new double[1][d + 1];
		for (int q = 0; q < d; q++) {
			ineqs[q][q + 1] = 1;
			ineqs[q + d][q + 1] = -1;
			ineqs[q][0] = -i[0][q];
			ineqs[q + d][0] = i[1][q];
			eqs[0][q + 1] = -1;
		}

		eqs[0][0] = 1;
		double[][] rays = p.getDoubleRays(eqs, ineqs);
		int nV = rays.length; // Number of vertices

		// Matrix with the coordinates of the nV vertices
		double[][] v = new double[nV][d];
		double sum;
		for (int j = 0; j < nV; j++) {
			sum = 0;
			for (int k = 0; k < d - 1; k++) {
				v[j][k] = rays[j][k + 1] / rays[j][0];
				v[j][k] = Math.round(v[j][k] * 100000d) / 100000d;
				sum += v[j][k];
			}
			v[j][d - 1] = Math.round((1.0 - sum) * 100000d) / 100000d;
		}

		return v;
	}

	/**
	 * Compute the extreme points of a probability interval using in d-1 dimensions.
	 *
	 * @param i a probability interval
	 * @return
	 */
	public static double[][] fromInt2VertSmallD(double[][] i) {
		// Using polco to compute vertices
		PolcoAdapter p;
		Options opts = new Options();
		opts.setLoglevel(Level.WARNING);
		try {
			p = new PolcoAdapter(opts);
		} catch (XmlConfigException e) {
			e.printStackTrace();
			throw new RuntimeException("An error occured while initializing polco", e);
		}
		int d = i[0].length;
		// Set linear constraints related to the first d-1 states
		// and an additional constraints about the first d-1 states
		// summing up to a number <=1 (because of normalization)
		// Those are 2*(d-1)+1=2d-1 constraints
		double[][] ineqs = new double[2 * d - 1][d];
		for (int q = 0; q < (d - 1); q++) {
			ineqs[q][q + 1] = 1;
			ineqs[q + (d - 1)][q + 1] = -1;
			ineqs[q][0] = -i[0][q];
			ineqs[q + (d - 1)][0] = i[1][q];
			ineqs[2 * d - 2][q + 1] = -1;
		}
		ineqs[2 * d - 2][0] = 1;
		double[][] rays = p.getDoubleRays(null, ineqs);
		int nV = rays.length; // Number of vertices
		// Matrix with the coordinates of the nV vertices
		double[][] v = new double[nV][d];
		double sum;
		for (int j = 0; j < nV; j++) {
			sum = 0;
			for (int k = 0; k < d - 1; k++) {
				v[j][k] = rays[j][k + 1] / rays[j][0];
				v[j][k] = Math.round(v[j][k] * 100000d) / 100000d;
				sum += v[j][k];
			}
			v[j][d - 1] = Math.round((1.0 - sum) * 100000d) / 100000d;
		}
		return v;
	}

	public double[][] JasperRandomGenerator(int dimension, double eps) {
		// TODO: eps not used
		double[] p = new double[dimension];
		double[] c = new double[dimension];
		double[] c2 = new double[dimension];
		double alpha = 0;
		double sum = 0.0;
		for (int j = 0; j < dimension; j++) {
			p[j] = Math.random();
			c[j] = Math.random();
			sum += p[j];
		}

		for (int j = 0; j < dimension; j++) {
			p[j] /= sum;
			c2[j] = 0;
			for (int i = 0; i < dimension; i++) {
				if (i != j) {
					c2[j] += c[i];
				}
			}
		}

		for (int j = 0; j < dimension; j++) {
			if (c2[j] > alpha) {
				alpha = c2[j];
			}
		}

		if (alpha > 1.0) {
			for (int i = 0; i < dimension; i++) {
				c[i] /= alpha;
			}
		}

		double[][] bounds = new double[2][dimension];
		for (int i = 0; i < dimension; i++) {
			bounds[0][i] = 0;
			bounds[1][i] = 1 - c[i];
		}

		// vertices = contaminate(vertices,p,.2);
		return fromInt2VertFullD(makeReachable(bounds));
	}

	// Compute the center of mass of a credal set
	public double[] fromVerticesToCoM(double[][] vertices) {
		// If a single vertex, the CoM is ...
		if (vertices.length == 1) {
			return vertices[0];
		} else {
			double[] cOm = new double[vertices[0].length];
			for (int j = 0; j < vertices[0].length; j++) {
				for (double[] vertex : vertices) {
					cOm[j] += vertex[j];
				}
				cOm[j] /= vertices.length;
			}
			ArraysUtil.roundArrayToTarget(cOm, 1.0, 1E-9);
			return cOm;
		}
	}

	/**
	 * Compute the center of mass of a credal set.
	 *
	 * @param bounds
	 * @return
	 */
	public double[] fromIntervalsToCoM(double[][] bounds) {
		double[] centerOfMass = new double[bounds[0].length];
		// CNGenerator myGen = new CNGenerator();
		// FIXME decide tolerance
		double eps = 1E-9;
		if (Arrays.stream(bounds[0]).sum() > (1.0 - eps)) {
			centerOfMass = bounds[0];
		} else {
			double[][] vertices = fromInt2VertFullD(bounds);
			centerOfMass = fromVerticesToCoM(vertices);
		}
		return centerOfMass;
	}

	/**
	 * @param dimensions number of dimensions
	 * @param datasize   number of observations, dataset size (to control the level of imprecision)
	 * @param ess        equivalent sample size, IDM parameter (to control the imprecision)
	 * @return
	 */
	public double[][] generate(int dimensions, int datasize, double ess) {
		// Initialization
		int[] counts = new int[dimensions]; // Pseudo-counts for the generation
		int n2 = 0;
		double[] values = new double[dimensions]; // Random values
		double[][] bounds = new double[2][dimensions];

		// double[] l = new double[d]; // Lower probabilities
		// double[] u = new double[d]; // Upper probabilities

		double sumValues = 0.0; // sum of random numbers

		// Generating d random numbers (double)
		for (int k = 0; k < dimensions; k++) {
			values[k] = Math.random();
			sumValues += values[k];
		}

		// Generating d integer numbers summing up to n
		for (int k = 0; k < dimensions; k++) {
			counts[k] = (int) Math.round(values[k] * datasize / sumValues);
			n2 += counts[k];
		}

		// Compute lower and upper probs (IDM)
		for (int k = 0; k < dimensions; k++) {
			bounds[0][k] = counts[k] / (n2 + ess);
			bounds[1][k] = (counts[k] + ess) / (n2 + ess);
		}

		// double[][] vertices2 = fromInt2VertFullD(makeReachable(bounds));
		return fromInt2VertSmallD(makeReachable(bounds));
	}

}