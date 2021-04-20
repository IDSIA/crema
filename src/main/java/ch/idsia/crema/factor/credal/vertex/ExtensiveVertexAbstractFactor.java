package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.vertex.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;

public class ExtensiveVertexAbstractFactor implements OperableFactor<ExtensiveVertexAbstractFactor> {

	protected ArrayList<double[]> data;
	protected Strides domain;
	protected final boolean log;

	public ExtensiveVertexAbstractFactor(Strides strides, boolean log, int capacity) {
		this.log = log;
		this.data = new ArrayList<>(capacity);
		this.domain = strides;
	}

	public ExtensiveVertexAbstractFactor(Strides strides, boolean log) {
		this.log = log;
		this.data = new ArrayList<>();
		this.domain = strides;
	}

	protected ExtensiveVertexAbstractFactor(Strides strides, ArrayList<double[]> data, boolean log) {
		this.data = data;
		this.domain = strides;
		this.log = log;
	}

	public void addInternalVertex(double[] data) {
		this.data.add(data);
	}

	public void addVertex(double[] data) {
		if (log) {
			for (int i = 0; i < data.length; ++i) {
				data[i] = Math.log(data[i]);
			}
		}
		this.data.add(data);
	}

	public void addVertex(BayesianFactor data) {
		if (this.log == data.isLog()) {
			this.data.add(data.getData());
		} else if (this.log) {
			// i'm not allowed to touch the internal array of the BayesianFactor
			double[] data_vertex = data.getData();
			double[] vertex = new double[data_vertex.length];
			for (int i = 0; i < vertex.length; ++i) {
				vertex[i] = Math.log(data_vertex[i]);
			}
			this.data.add(vertex);
		} else {
			this.data.add(data.getData());
		}
	}

	public BayesianFactor getBayesianVertex(int vertex) {
		// TODO: log version
		return new BayesianDefaultFactor(domain, data.get(vertex));
	}

	@Override
	public Strides getDomain() {
		return domain;
	}

	public ArrayList<double[]> getInternalVertices() {
		return data;
	}

	public boolean isLog() {
		return log;
	}

	@Override
	public ExtensiveVertexAbstractFactor copy() {
		ArrayList<double[]> new_data = new ArrayList<>(data.size());
		for (double[] vertex : data) {
			new_data.add(vertex.clone());
		}
		return new ExtensiveVertexAbstractFactor(domain, new_data, log);
	}

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

	@Override
	public ExtensiveVertexAbstractFactor combine(ExtensiveVertexAbstractFactor factor) {
		final Strides target = domain.union(factor.domain);
		final int length = target.getSize();

		final int[] limits = new int[length];

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

		ExtensiveVertexAbstractFactor target_factor = new ExtensiveVertexAbstractFactor(target, log);
		target_factor.data.ensureCapacity(our_tables * his_tables);
		final int table_size = target.getCombinations();

		LogVertexOperation ops = new LogVertexOperation();
		for (int our_table = 0; our_table < our_tables; ++our_table) {
			for (int his_table = 0; his_table < his_tables; ++his_table) {

				final double[] result = new double[table_size];
				final double[] our_data = data.get(our_table);
				final double[] his_data = factor.data.get(his_table);
				target_factor.addInternalVertex(result);

				ops.combine(our_data, his_data, table_size, stride, reset, limits);
			}
		}

		return target_factor;
	}

	@Override
	public ExtensiveVertexAbstractFactor filter(int variable, int state) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Filter(domain.getStrideAt(offset), state));
	}

	@Override
	public ExtensiveVertexAbstractFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (log)
			return collect(offset, new LogMarginal(domain.getSizeAt(offset), domain.getStrideAt(offset)));
		else
			return collect(offset, new Marginal(domain.getSizeAt(offset), domain.getStrideAt(offset)));
	}

	private ExtensiveVertexAbstractFactor collect(final int offset, final Collector collector) {

		final int stride = domain.getStrideAt(offset);
		final int size = domain.getSizeAt(offset);
		final int reset = size * stride;

		Strides target_domain = domain.removeAt(offset); //new Strides(domain, offset);

		ExtensiveVertexAbstractFactor result = new ExtensiveVertexAbstractFactor(target_domain, isLog());
		for (double[] vertex : this.data) {
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

			result.data.add(newData);
		}

		return result;
	}

	@Override
	public ExtensiveVertexAbstractFactor divide(ExtensiveVertexAbstractFactor factor) {
		// TODO
		throw new NotImplementedException();
	}
}
