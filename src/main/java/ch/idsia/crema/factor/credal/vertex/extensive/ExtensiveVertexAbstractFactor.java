package ch.idsia.crema.factor.credal.vertex.extensive;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.collectors.Collector;
import ch.idsia.crema.factor.algebra.vertex.VertexOperation;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ExtensiveVertexAbstractFactor implements ExtensiveVertexFactor {

	protected Strides domain;

	/**
	 * @param domain the domain of this new factor
	 */
	ExtensiveVertexAbstractFactor(Strides domain) {
		this.domain = domain;
	}

	/**
	 * @return the domain associated with this factor
	 */
	@Override
	public Strides getDomain() {
		return domain;
	}

	/*
	public ExtensiveVertexAbstractFactor combine2(ExtensiveVertexAbstractFactor factor) {
		final Strides target = domain.union(factor.domain);
		final int length = target.getSize();

		final int[] limits = new int[length];
		final int[] assign = new int[length];

		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < domain.getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), domain.getVariables()[vindex]);
			if (offset >= 0) {
				stride[offset] = domain.getStrides()[vindex];
			}
		}

		for (int vindex = 0; vindex < factor.domain.getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), factor.domain.getVariables()[vindex]);
			if (offset >= 0) {
				stride[offset] += ((long) factor.domain.getStrides()[vindex] << 32L);
			}
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = target.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		final int our_tables = data.size();
		final int his_tables = factor.data.size();

		int tables = our_tables * his_tables;

		long idx = 0;

		double[][] result = new double[tables][target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {

			int table = 0;
			for (int our_table = 0; our_table < our_tables; ++our_table) {
				for (int his_table = 0; his_table < his_tables; ++his_table) {
					result[table++][i] = data.get(our_table)[(int) (idx & 0xFFFFFFFF)] + factor.data.get(his_table)[(int) (idx >>> 32L)];
				}
			}

			for (int l = 0; l < length; ++l) {
				if (assign[l] == limits[l]) {
					assign[l] = 0;
					idx -= reset[l];
				} else {
					++assign[l];
					idx += stride[l];
					break;
				}
			}
		}

		ExtensiveVertexAbstractFactor target_factor = new ExtensiveVertexAbstractFactor(target, log);
		target_factor.data.addAll(Arrays.asList(result));
		return target_factor;
	}
	*/

	/**
	 * @param factor  factor to combine with
	 * @param builder output factor constructor
	 * @param ops     {@link VertexOperation} to use
	 * @param <T>     returned type
	 * @return a new factor, combination of this with the given one
	 */
	protected <T extends ExtensiveVertexAbstractFactor> T combine(
			ExtensiveVertexFactor factor,
			ExtensiveVertexFactorBuilder<T> builder,
			VertexOperation ops
	) {
		final Strides target = this.getDomain().union(factor.getDomain());
		final int length = target.getSize();

		final int[] limits = new int[length];
		final long[] stride = new long[length];
		final long[] reset = new long[length];

		for (int vindex = 0; vindex < this.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), this.getDomain().getVariables()[vindex]);
			if (offset >= 0) {
				stride[offset] = this.getDomain().getStrides()[vindex];
			}
		}

		for (int vindex = 0; vindex < factor.getDomain().getSize(); ++vindex) {
			int offset = Arrays.binarySearch(target.getVariables(), factor.getDomain().getVariables()[vindex]);
			if (offset >= 0) {
				stride[offset] += ((long) factor.getDomain().getStrides()[vindex] << 32L);
			}
		}

		for (int i = 0; i < length; ++i) {
			limits[i] = target.getSizes()[i] - 1;
			reset[i] = limits[i] * stride[i];
		}

		final int our_tables = size();
		final int his_tables = factor.size();

		final int table_size = target.getCombinations();

		List<double[]> vertices = new ArrayList<>();

		for (int our_table = 0; our_table < our_tables; ++our_table) {
			for (int his_table = 0; his_table < his_tables; ++his_table) {
				final double[] our_data = getInternalVertices().get(our_table);
				final double[] his_data = factor.getInternalVertices().get(his_table);

				final double[] result = ops.combine(our_data, his_data, table_size, stride, reset, limits);
				vertices.add(result);
			}
		}

		return builder.get(target, vertices);
	}

	/**
	 * @param offset    offset to collect
	 * @param collector {@link Collector} operation to use
	 * @param builder   output factor constructor
	 * @param <T>       returned type
	 * @return a new factor, result of the collection operation used
	 */
	protected <T extends ExtensiveVertexAbstractFactor> T collect(
			final int offset,
			final Collector collector,
			ExtensiveVertexFactorBuilder<T> builder
	) {
		final int stride = getDomain().getStrideAt(offset);
		final int size = getDomain().getSizeAt(offset);
		final int reset = size * stride;

		final Strides target_domain = getDomain().removeAt(offset); // new Strides(domain, offset);

		final List<double[]> vertices = new ArrayList<>();

		for (double[] vertex : getInternalVertices()) {
			int source = 0;
			int next = stride;
			int jump = stride * (size - 1);

			final double[] newData = new double[target_domain.getCombinations()];

			for (int target = 0; target < target_domain.getCombinations(); ++target, ++source) {
				if (source == next) {
					source += jump;
					next += reset;
				}

				newData[target] = collector.collect(vertex, source);
			}

			vertices.add(newData);
		}

		return builder.get(target_domain, vertices);
	}

	@Override
	public ExtensiveVertexAbstractFactor divide(ExtensiveVertexFactor factor) {
		// TODO
		throw new NotImplementedException();
	}
}
