package ch.idsia.crema.factor.credal.vertex.generator;

import org.junit.Test;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.12.2020 11:27
 */
public class CNGeneratorTest {

	/**
	 * Tool to display 2d-arrays.
	 *
	 * @param matrix 2d-array
	 */
	public void printMatrix(double[][] matrix) {
		for (double[] doubles : matrix) {
			for (double aDouble : doubles) {
				System.out.print(aDouble + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Tool to display 1d-arrays.
	 *
	 * @param array 1d-array
	 */
	public void printArray(double[] array) {
		for (double value : array) {
			System.out.print(value + " ");
		}
		System.out.println();
	}

	@Test
	public void testSimpleGenerator() {
		CNGenerator a = new CNGenerator();
		CNGenerator reachRobot = new CNGenerator();

		double[] lowerP = {.2, .1};
		double[] upperP = {.95, .7};
		double[][] myBounds = {lowerP, upperP};
		double[][] myReachBounds = reachRobot.makeReachable(myBounds);

		System.out.println("Before");
		System.out.println("L=" + Arrays.toString(myBounds[0]));
		System.out.println("U=" + Arrays.toString(myBounds[1]));
		System.out.println("After");
		System.out.println("L=" + Arrays.toString(myReachBounds[0]));
		System.out.println("U=" + Arrays.toString(myReachBounds[1]));

		double[][] v = a.linvac(5, .1);
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

	@Test
	public void test3d() {
		CNGenerator generator = new CNGenerator();
		double[][] matrix = generator.generate(3, 100, 2);
	}

}