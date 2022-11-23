package ch.idsia.crema.factor.algebra;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.collectors.Collector;
import ch.idsia.crema.factor.algebra.collectors.Filter;
import ch.idsia.crema.factor.algebra.collectors.LogMarginal;
import ch.idsia.crema.factor.algebra.collectors.Marginal;
import ch.idsia.crema.factor.algebra.vertex.LogVertexOperation;
import ch.idsia.crema.factor.algebra.vertex.SimpleVertexOperation;
import ch.idsia.crema.factor.algebra.vertex.VertexOperation;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexDefaultFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexFactor;
import ch.idsia.crema.factor.credal.vertex.extensive.ExtensiveVertexLogFactor;
import ch.idsia.crema.utility.ArraysUtil;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtensiveVertexDefaultAlgebra implements Operation<ExtensiveVertexFactor> {

	protected ExtensiveVertexFactor collect(final ExtensiveVertexFactor factor, final int offset, final Collector collector) {
		final Strides domain = factor.getDomain();
		final int stride = domain.getStrideAt(offset);
		final int size = domain.getSizeAt(offset);
		final int jump = stride * (size - 1);
		final int reset = size * stride;

		final Strides target_domain = domain.removeAt(offset);

		List<double[]> vertices = new ArrayList<>();

		// marginalize all the vertices of the source factor
		for (double[] vertex : factor.getInternalVertices()) {
			int source = 0;
			int next = stride;

			final double[] new_data = new double[target_domain.getCombinations()];

			for (int target = 0; target < target_domain.getCombinations(); ++target, ++source) {
				if (source == next) {
					source += jump;
					next += reset;
				}

				new_data[target] = collector.collect(vertex, source);
			}

			vertices.add(new_data);
		}

		if (factor.isLog())
			return new ExtensiveVertexLogFactor(target_domain, vertices);
		else
			return new ExtensiveVertexDefaultFactor(target_domain, vertices);
	}

	@Override
	public ExtensiveVertexFactor combine(ExtensiveVertexFactor one, ExtensiveVertexFactor two) {
		final boolean oneIsLog = one.isLog();
		final boolean twoIsLog = two.isLog();

		final VertexOperation ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleVertexOperation();
		} else {
			ops = new LogVertexOperation();

			if (!oneIsLog)
				one = new ExtensiveVertexLogFactor((ExtensiveVertexDefaultFactor) one);
			if (!twoIsLog)
				two = new ExtensiveVertexLogFactor((ExtensiveVertexDefaultFactor) two);
		}

		final Strides target = one.getDomain().union(two.getDomain());
		final int length = target.getSize();

		final int[] limits = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < one.getDomain().getSize(); ++vindex) {
			int offset = ArraysUtil.indexOf(one.getDomain().getVariables()[vindex], target.getVariables());
			if (offset >= 0) {
				stride[offset] = one.getDomain().getStrides()[vindex];
			}
		}

		for (int vindex = 0; vindex < two.getDomain().getSize(); ++vindex) {
			int offset = ArraysUtil.indexOf(two.getDomain().getVariables()[vindex], target.getVariables());
			if (offset >= 0) {
				stride[offset] += ((long) two.getDomain().getStrides()[vindex] << 32L);
			}
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = target.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		final int our_tables = one.getInternalVertices().size();
		final int his_tables = two.getInternalVertices().size();

		final int table_size = target.getCombinations();

		List<double[]> vertices = new ArrayList<>();

		for (int our_table = 0; our_table < our_tables; ++our_table) {
			for (int his_table = 0; his_table < his_tables; ++his_table) {
				final double[] our_data = one.getInternalVertices().get(our_table);
				final double[] his_data = two.getInternalVertices().get(his_table);

				final double[] result = ops.combine(our_data, his_data, table_size, stride, reset, limits);
				vertices.add(result);
			}
		}

		if (oneIsLog && twoIsLog)
			return new ExtensiveVertexLogFactor(target, vertices);
		else
			return new ExtensiveVertexDefaultFactor(target, vertices);
	}

	@Override
	public ExtensiveVertexFactor filter(final ExtensiveVertexFactor factor, int variable, int state) {
		final Strides domain = factor.getDomain();
		int offset = domain.indexOf(variable);
		return collect(factor, offset, new Filter(domain.getStrideAt(offset), state));
	}

	@Override
	public ExtensiveVertexFactor marginalize(final ExtensiveVertexFactor factor, int variable) {
		final Strides domain = factor.getDomain();
		final int offset = domain.indexOf(variable);

		if (factor.isLog())
			return collect(factor, offset, new LogMarginal(domain.getSizeAt(offset), domain.getStrideAt(offset)));
		else
			return collect(factor, offset, new Marginal(domain.getSizeAt(offset), domain.getStrideAt(offset)));
	}

	@Override
	public ExtensiveVertexFactor divide(ExtensiveVertexFactor one, ExtensiveVertexFactor other) {
		// TODO
		throw new NotImplementedException();
	}

}
