package ch.idsia.crema.factor.algebra.vertex;

/**
 * @author david
 * @author claudio
 */
public interface VertexOperation {

	/**
	 * @param t1
	 * @param t2
	 * @param size
	 * @param stride
	 * @param reset
	 * @param limits
	 * @return
	 */
	double[] combine(double[] t1, double[] t2, int size, long[] stride, long[] reset, int[] limits);

	double[] marginalize(double[] data, int size, int stride);

}
