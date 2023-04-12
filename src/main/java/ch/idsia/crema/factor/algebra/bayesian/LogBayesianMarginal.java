package ch.idsia.crema.factor.algebra.bayesian;

import ch.idsia.crema.factor.algebra.OperationUtils;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

/**
 * A collector to marginalize a variable out of a domain in logspace.
 * 
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    21.04.2021 21:23
 */
public class LogBayesianMarginal implements BayesianCollector {

	private final int[] offsets;
	private final int size;

	/**
	 * Construct the collector that summs all values of the variable.
	 * This will compute the set of offsets defined by the strides. 
	 * 
	 * @param size the size of the variable to be collected
	 * @param stride the stride of the variable.
	 */
	public LogBayesianMarginal(int size, int stride) {
		this.size = size;
		offsets = new int[size];
		
		// we can safely start from 1 as the index 0 is always 0.
		for (int i = 1; i < size; ++i) {
			offsets[i] = i * stride;
		}
	}

	@Override
	public final double collect(BayesianFactor factor, int source) {
		// 270 slowest!!
		// return Arrays.stream(offsets).map(v->v+source)
		// 		.mapToDouble(factor::getLogValueAt)
		// 		.reduce(Double.NEGATIVE_INFINITY, OperationUtils::logSum);
		
		// // 130
		double value = factor.getLogValueAt(source + offsets[0]); 
		for (int i = 1; i < size; ++i) {
			double v = factor.getLogValueAt(source + offsets[i]); 

			if (v > value) {
				value = v + Math.log1p(FastMath.exp(value - v));
			} else {
				value += Math.log1p(FastMath.exp(v - value));
			}
		}
		return value;

		// // 226
		// double value = Double.NEGATIVE_INFINITY;
		// for (int i = 0; i < size; ++i) {
		// 	double v = factor.getLogValueAt(source + offsets[i]); 
		// 	value = OperationUtils.logSum(value, v);
		// }
		// return value;

		// 95 but wrong
 		// double value = factor.getValueAt(source + offsets[0]);
		// for (int i = 1; i < size; ++i) {
		// 	value += factor.getValueAt(source + offsets[i]); // TODO: try with ch.idsia.crema.factor.algebra.OperationUtils.logSum()
		// }
		// return FastMath.log(value);
	}
}
