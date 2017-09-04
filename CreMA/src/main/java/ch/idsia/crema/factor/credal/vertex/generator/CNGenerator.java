package ch.idsia.crema.factor.credal.vertex.generator;

import ch.idsia.crema.utility.ArraysUtil;
import ch.javasoft.polco.adapter.Options;
import ch.javasoft.polco.adapter.PolcoAdapter;
import ch.javasoft.xml.config.XmlConfigException;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;

public class CNGenerator {
	static Random random = new Random(0);

	public static double[] randomMassFunction(int dimension) {
		int d = dimension;
		double[] p = new double[d];
		double sum = 0.0;
		for (int j = 0; j < d; j++) {
			p[j] = random.nextDouble();//Math.random();
			sum += p[j];
		}
		for (int j = 0; j < d; j++) {
			p[j] /= sum;
		}
		return p;
	}

	public double[][] linvac(int dimension, double epsilon) {
		int d = dimension;
		double e = epsilon;
		double sum;
		double[][] v2 = new double[d][d]; // first d is # vertices
		double[] p = randomMassFunction(d);
		for (int i = 0; i < d; i++) {
			sum = 0.0;
			for (int j = 0; j < (d - 1); j++) {
				if (j == i) {
					v2[i][j] = e + (1 - e) * p[j];
				} else {
					v2[i][j] = (1 - e) * p[j];
				}
				v2[i][j] = Math.round(v2[i][j] * 100000d) / 100000d;
				sum += v2[i][j];
			}
			v2[i][d - 1] = Math.round((1.0 - sum) * 100000d) / 100000d;
		}
		return v2;
	}

	// Tool to display 2d-arrays
	public static void printMatrix(double[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				System.out.print(m[i][j] + " ");
			}
			System.out.println();
		}
	}

	// Tool to display 2d-arrays
	public static void printArray(double[] v) {
		for (int i = 0; i < v.length; i++) {
			System.out.print(v[i] + " ");
		}
		System.out.println();
	}

	// Convert probability intervals into reachable ones
	public double[][] makeReachable(double[][] bounds) {
		// DEBUG: add non-emptiness check
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

	// Compute the extreme points of a probability interval
	// using the whole dimensionality
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

	// Compute the extreme points of a probability interval
	// using in d-1 dimensions
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

	public double[][] JasperRandomGenerator(int dimension, double epsilon) {
		int d = dimension;
		double e = epsilon;
		double[] p = new double[d];
		double[] c = new double[d];
		double[] c2 = new double[d];
		double alpha = 0;
		double sum = 0.0;
		for (int j = 0; j < d; j++) {
			p[j] = Math.random();
			c[j] = Math.random();
			sum += p[j];
		}
		for (int j = 0; j < d; j++) {
			p[j] /= sum;
			c2[j] = 0;
			for (int i = 0; i < d; i++) {
				if (i != j) {
					c2[j] += c[i];
				}
			}
		}
		for (int j = 0; j < d; j++) {
			if (c2[j] > alpha) {
				alpha = c2[j];
			}
		}
		if (alpha > 1.0) {
			for (int i = 0; i < d; i++) {
				c[i] /= alpha;
			}
		}
		double[][] bounds = new double[2][d];
		for (int i = 0; i < d; i++) {
			bounds[0][i] = 0;
			bounds[1][i] = 1 - c[i];
		}
		double[][] vertices = fromInt2VertFullD(makeReachable(bounds));
		// vertices = contaminate(vertices,p,.2);
		return vertices;
	}

	// Compute the center of mass of a credal set
	public double[] fromVerticesToCoM(double[][] vertices){
		// If a single vertex, the CoM is ...
		if(vertices.length==1){ return vertices[0]; }
		else{
			double[] cOm = new double[vertices[0].length];
			for(int j=0;j<vertices[0].length;j++){
				for(int i=0;i<vertices.length;i++){
					cOm[j] += vertices[i][j];}
				cOm[j] /= vertices.length;}
			ArraysUtil.roundArrayToTarget(cOm,1.0,1E-9);
			return cOm;
		}}

	// Compute the center of mass of a credal set
	public double[] fromIntervalsToCoM(double[][] bounds){
		double[] centerOfMass = new double[bounds[0].length];
		//CNGenerator myGen = new CNGenerator();
		// FIXME decide tolerance
		double eps = 1E-9;
		if(Arrays.stream(bounds[0]).sum()>(1.0-eps)){
			centerOfMass = bounds[0];}
		else{
			double[][] vertices = fromInt2VertFullD(bounds);
			centerOfMass = fromVerticesToCoM(vertices);}
		return centerOfMass;}





	public double[][] generate(int dimensions, int datasize, double ess) {
		// Parameters
		int d = dimensions; // Dimension
		int n = datasize; // Dataset size (to control the level of imprecision)
		double s = ess; // IDM parameter (to control the imprecision)
		// Initialization
		int[] counts = new int[d]; // Pseudo-counts for the generation
		int n2 = 0;
		double[] values = new double[d]; // Random values
		double[][] bounds = new double[2][d];
		// double[] l = new double[d]; // Lower probabilities
		// double[] u = new double[d]; // Upper probabilities
		double sumValues = 0.0; // sum of random numbers

		// Generating d random numbers (double)
		for (int k = 0; k < d; k++) {
			values[k] = Math.random();
			sumValues += values[k];
		}
		// Generating d integer numbers summing up to n
		for (int k = 0; k < d; k++) {
			counts[k] = (int) Math.round(values[k] * n / sumValues);
			n2 += counts[k];
		}
		// Compute lower and upper probs (IDM)
		for (int k = 0; k < d; k++) {
			bounds[0][k] = counts[k] / (n2 + s);
			bounds[1][k] = (counts[k] + s) / (n2 + s);
		}
		double[][] vertices = fromInt2VertSmallD(makeReachable(bounds));
		// double[][] vertices2 = fromInt2VertFullD(makeReachable(bounds));
		return vertices;
	}




	public static void main(String[] args) {

		CNGenerator a = new CNGenerator();
		
		double[] lowerP = {.2,.1};
		double[] upperP = {.95,.7};
		double[][] myBounds = {lowerP,upperP};
		double[][] myReachBounds = new double[2][lowerP.length];
		CNGenerator reachRobot = new CNGenerator();
		myReachBounds = reachRobot.makeReachable(myBounds);
		System.out.println("Before");
		System.out.println("L="+Arrays.toString(myBounds[0]));
		System.out.println("U="+Arrays.toString(myBounds[1]));
		System.out.println("After");
		System.out.println("L="+Arrays.toString(myReachBounds[0]));
		System.out.println("U="+Arrays.toString(myReachBounds[1]));

		
		
		double[][] v = a.linvac(5,.1);
		//double[][] z = a.JasperRandomGenerator(3,.1);
		//double[][] v;
		//v = a.Jasper(3, .01);
		// double[][] v = a.generate(3, 10, 2.0);
		printMatrix(v);
		//printMatrix(z);

		// double[][] v2 = a.generate2(3, 10, 2.0);
		// printMatrix(v2);
		// double[][] bounds = {{.1,.1,.1},{.8,.5,.8}};
		// double[][] bounds = {{.1,.2,.1},{.7,.8,.9}};
		// printMatrix(bounds);
		// System.out.println("---");
		// bounds = makeReachable(bounds);
		// System.out.println("---");

		// printMatrix(fromInt2VertSmallD(bounds));
		// System.out.println("---");
		// printMatrix(fromInt2VertFullD(bounds));

		// double[][] matrice = {{.2,.3,.5},{.3,.4,.3},{.1,.8,.1}};
		// a.setBounds(bounds);
		// a.vertices = matrice;
		// printMatrix(new CNGenerator().generate(3, 10, 2));
	}
}
/**
 * generate a set of vertices
 * 
 * @param dimensions
 *            - number of dimensions
 * @param datasize
 *            - number of observations
 * @param ess
 *            - equivalent sample size
 * @return
 */