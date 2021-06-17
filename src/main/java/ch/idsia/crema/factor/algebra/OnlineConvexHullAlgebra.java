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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: rename to InlineConvexHullAlgebra?
public abstract class OnlineConvexHullAlgebra implements Operation<ExtensiveVertexFactor> {

	// TODO: the only implementation is a trivial hash-based check. Implement it?
	protected abstract boolean canAddVector(List<double[]> vertex, double[] vector);

	@Override
	public ExtensiveVertexFactor combine(ExtensiveVertexFactor one, ExtensiveVertexFactor two) {
		final boolean oneIsLog = one.isLog();
		final boolean twoIsLog = two.isLog();

		VertexOperation ops;

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
			int offset = Arrays.binarySearch(target.getVariables(), one.getDomain().getVariables()[vindex]);
			if (offset >= 0) {
				stride[offset] = one.getDomain().getStrides()[vindex];
			}
		}

		for (int vindex = 0; vindex < two.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), two.getDomain().getVariables()[vindex]);
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

		List<double[]> vertices = new ArrayList<>();

		final int table_size = target.getCombinations();

		for (int our_table = 0; our_table < our_tables; ++our_table) {
			for (int his_table = 0; his_table < his_tables; ++his_table) {
				final double[] our_data = one.getInternalVertices().get(our_table);
				final double[] his_data = two.getInternalVertices().get(his_table);

				final double[] result = ops.combine(our_data, his_data, table_size, stride, reset, limits);

				if (canAddVector(vertices, result)) {
					vertices.add(result);
				}
			}
		}

		if (oneIsLog && twoIsLog)
			return new ExtensiveVertexLogFactor(target, vertices);
		else
			return new ExtensiveVertexDefaultFactor(target, vertices);
	}

	@Override
	public ExtensiveVertexFactor filter(final ExtensiveVertexFactor factor, int variable, int state) {
		int offset = factor.getDomain().indexOf(variable);
		return collect(factor, offset, new Filter(factor.getDomain().getStrideAt(offset), state));
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

			final double[] newData = new double[target_domain.getCombinations()];

			for (int target = 0; target < target_domain.getCombinations(); ++target, ++source) {
				if (source == next) {
					source += jump;
					next += reset;
				}

				newData[target] = collector.collect(vertex, source);
			}

			// ask if the vertex should be added
			if (canAddVector(vertices, newData)) {
				vertices.add(newData);
			}
		}

		if (factor.isLog())
			return new ExtensiveVertexLogFactor(target_domain, vertices);
		else
			return new ExtensiveVertexDefaultFactor(target_domain, vertices);
	}

	@Override
	public ExtensiveVertexFactor divide(ExtensiveVertexFactor one, ExtensiveVertexFactor other) {
		// TODO
		throw new UnsupportedOperationException();
	}
}
