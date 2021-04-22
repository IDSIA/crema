package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.credal.linear.interval.IntervalDefaultFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Convert from {@link VertexFactor} to {@link IntervalFactor}
 *
 * @author huber
 */
public class VertexToInterval implements Converter<VertexFactor, IntervalFactor> {

	@Override
	public IntervalFactor apply(VertexFactor s, Integer var) {
		int dimensions = s.getDataDomain().getCombinations();

		double[] lower_template = new double[dimensions];
		Arrays.fill(lower_template, Double.POSITIVE_INFINITY);

		double[] upper_template = new double[dimensions];
		Arrays.fill(upper_template, Double.NEGATIVE_INFINITY);

		final int separate_size = s.getSeparatingDomain().getCombinations();
		double[][] lowers = new double[separate_size][];
		double[][] uppers = new double[separate_size][];

		for (int i = 0; i < separate_size; ++i) {
			double[][] vertices = s.getVerticesAt(i);
			double[] lower = lowers[i] = ArrayUtils.clone(lower_template);
			double[] upper = uppers[i] = ArrayUtils.clone(upper_template);

			for (double[] vertex : vertices) {
				for (int coord = 0; coord < dimensions; ++coord) {
					lower[coord] = Math.min(lower[coord], vertex[coord]);
					upper[coord] = Math.max(upper[coord], vertex[coord]);
				}
			}
		}
		return new IntervalDefaultFactor(s.getDataDomain(), s.getSeparatingDomain(), lowers, uppers);
	}

	@Override
	public Class<IntervalFactor> getTargetClass() {
		return IntervalFactor.class;
	}

	@Override
	public Class<VertexFactor> getSourceClass() {
		return VertexFactor.class;
	}

}
