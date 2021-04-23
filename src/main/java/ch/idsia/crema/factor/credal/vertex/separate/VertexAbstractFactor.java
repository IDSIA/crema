package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import ch.idsia.crema.utility.SeparateIndexIterator;
import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

/**
 * A Separately specified Vertex based credal factor.
 *
 * @author david
 */
// TODO: Data is currently not logged!
public abstract class VertexAbstractFactor implements VertexFactor {
	protected final Strides separatedDomain;
	protected final Strides vertexDomain;

	public static boolean CONVEX_HULL_MARG = false;

	public VertexAbstractFactor(Strides vertexDomain, Strides separatedDomain) {
		this.separatedDomain = separatedDomain;
		this.vertexDomain = vertexDomain;
	}

	@Override
	public Strides getDomain() {
		return separatedDomain.union(vertexDomain);
	}

	@Override
	public Strides getDataDomain() {
		return vertexDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return separatedDomain;
	}

	/**
	 * Iterate over this object's domains extended to the specified domain
	 *
	 * @param domain
	 * @return
	 */
	private SeparateIndexIterator iterate(Strides domain) {
		IndexIterator data = vertexDomain.getIterator(domain);
		IndexIterator group = separatedDomain.getIterator(domain);
		return new SeparateIndexIterator(data, group);
	}

	/**
	 * Iterator over the two domains data and conditioning
	 *
	 * @return
	 */
	public SeparateIndexIterator iterate() {
		IndexIterator data = new IndexIterator(vertexDomain);
		IndexIterator group = new IndexIterator(separatedDomain);
		return new SeparateIndexIterator(data, group);
	}

	protected <F extends VertexAbstractFactor> F filter(int variable, int state, VertexFactorBuilder<F> builder) {
		int var_offset = separatedDomain.indexOf(variable);
		if (var_offset < 0) {
			double[][][] newdata = new double[separatedDomain.getCombinations()][][];
			var_offset = vertexDomain.indexOf(variable);
			Strides newleft = vertexDomain.removeAt(var_offset);

			int offset = vertexDomain.getPartialOffset(new int[]{variable}, new int[]{state});

			for (int r = 0; r < separatedDomain.getCombinations(); ++r) {
				double[][] source = getVerticesAt(r); // TODO: put data[r] back?
				double[][] target = new double[source.length][newleft.getCombinations()];
				newdata[r] = target;

				IndexIterator iter = vertexDomain.getIterator(newleft);

				int tindex = 0;
				while (iter.hasNext()) {
					int i = iter.next();
					for (int vertex = 0; vertex < source.length; ++vertex) {
						target[vertex][tindex] = source[vertex][i + offset];
					}
					++tindex;
				}
			}

			return builder.get(newleft, separatedDomain, newdata);
		} else {
			Strides newdomain = separatedDomain.removeAt(var_offset); // new Strides(separatedDomain, var_offset);
			IndexIterator iter = separatedDomain.getFiteredIndexIterator(variable, state);

			// should be replaceable with
			// int offset = separatedDomain.getPartialOffset(new int[] {
			// variable },
			// new int[] { state });
			// IndexIterator iter = separatedDomain.getIterator(separatedDomain,
			// variable);

			double[][][] newdata = new double[newdomain.getCombinations()][][];
			for (int index = 0; index < newdata.length; ++index) {
				newdata[index] = getVerticesAt(iter.next()); // TODO: put data[iter.next()] back?
			}
			return builder.get(vertexDomain, newdomain, newdata);
		}
	}

	/**
	 * sum/marginalize some variables out of the credal set.
	 *
	 * @param builder
	 * @param vars
	 * @return
	 */
	protected <F extends VertexAbstractFactor> F marginalize(VertexFactorBuilder<F> builder, int... vars) {
		// only vars of the domain
		Strides sum_strides = getDataDomain().intersection(vars);
		Strides left = getDataDomain().remove(vars);
		double[][][] target_data = new double[size()][][];
		for (int c = 0; c < size(); ++c) {
			double[][] source = getVerticesAt(c); // TODO: double[][] source = data[c];
			double[][] target = new double[source.length][left.getCombinations()];
			target_data[c] = target;

			IndexIterator src_remaining = getDataDomain().getIterator(left);
			IndexIterator src_summing = getDataDomain().getIterator(sum_strides, left.getVariables());
			for (int d = 0; d < left.getCombinations(); ++d) {
				int src = src_remaining.next();
				IndexIterator i = src_summing.clone();
				while (i.hasNext()) {
					int offset = i.next();
					for (int v = 0; v < source.length; ++v) {
						// FIXME this should be abstracted to support logsum
						target[v][d] += source[v][src + offset];
					}
				}
			}
		}

		F f = builder.get(left, getSeparatingDomain(), target_data);

		if (CONVEX_HULL_MARG)
			f.applyConvexHull();

		return f;
	}

	/**
	 * @param target the new grouping/separation domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <F extends VertexAbstractFactor> F reseparate(Strides target, VertexFactorBuilder<F> builder) {
		// requested current separation!
		if (Arrays.equals(target.getVariables(), separatedDomain.getVariables())) return (F) this;

		Strides T = getSeparatingDomain().intersection(target);
		Strides Lt = getSeparatingDomain().remove(target);
		Strides Dl = getDataDomain().union(Lt);

		// target data
		double[][][] dest_data = new double[T.getCombinations()][][];

		// lets first iterate over the part of the grouping that stays the same
		IndexIterator src_right_offset_iter = getSeparatingDomain().getIterator(T);
		for (int dest_right = 0; dest_right < T.getCombinations(); ++dest_right) {
			int src_right_1 = src_right_offset_iter.next();

			// count vectors
			IndexIterator iter = getSeparatingDomain().getIterator(Lt);

			int[] vertex_counts = new int[Lt.getCombinations()];
			int index = 0;
			while (iter.hasNext()) {
				int idx = src_right_1 + iter.next();
				vertex_counts[index++] = getVerticesAt(idx).length; // TODO: vertex_counts[index++] = data[idx].length;
			}

			int[] vvars = IntStream.range(0, index).toArray();
			Strides vstride = new Strides(vvars, vertex_counts);
			dest_data[dest_right] = new double[vstride.getCombinations()][Dl.getCombinations()];

			int vertex = 0;
			IndexIterator viter = vstride.getIterator(vstride);
			while (viter.hasNext()) {
				// get the list of selected vertices combination
				int[] locations = viter.getPositions();

				IndexIterator src_right_iter = getSeparatingDomain().getIterator(Dl, T.getVariables());
				IndexIterator src_left_iter = getDataDomain().getIterator(Dl);
				IndexIterator vindex = Lt.getIterator(Dl);

				for (int i = 0; i < Dl.getCombinations(); ++i) {
					int offset = vindex.next();

					int src_l = src_left_iter.next();
					int src_r_2 = src_right_iter.next();
					int src_r = src_r_2 + src_right_1;

					double v = getVerticesAt(src_r)[locations[offset]][src_l]; // TODO: double v = data[src_r][locations[offset]][src_l];

					dest_data[dest_right][vertex][i] = v;
				}

				viter.next();
				++vertex;
			}
		}

		return builder.get(Dl, T, dest_data);
	}

	protected <F extends VertexAbstractFactor> F combine(VertexFactor other, VertexFactorBuilder<F> builder, ToDoubleBiFunction<Double, Double> multiply) {
		if (!this.getDomain().isConsistentWith(other.getDomain())) {
			throw new IllegalArgumentException("Factors domains are not consistent: " + this + ", " + other);
		}

		// union
		Strides left = getDataDomain().union(other.getDataDomain());
		Strides runion = getSeparatingDomain().union(other.getSeparatingDomain());
		Strides right = runion.remove(left);

		VertexFactor reshaped1 = this.reseparate(right);
		VertexFactor reshaped2 = other.reseparate(right);

		double[][][] target_data = new double[right.getCombinations()][][];

		IndexIterator iter1 = reshaped1.getSeparatingDomain().getIterator(right);
		IndexIterator iter2 = reshaped2.getSeparatingDomain().getIterator(right);

		for (int r = 0; r < right.getCombinations(); ++r) {
			int idx1 = iter1.next();
			int idx2 = iter2.next();
			double[][] data1 = reshaped1.getVerticesAt(idx1); // TODO: double[][] data1 = reshaped1.getVerticesAt(idx1);
			double[][] data2 = reshaped2.getVerticesAt(idx2); // TODO: double[][] data2 = reshaped2.getData()[idx2];
			double[][] target = new double[data1.length * data2.length][left.getCombinations()];
			target_data[r] = target;

			for (int v1 = 0; v1 < data1.length; ++v1) {
				for (int v2 = 0; v2 < data2.length; ++v2) {
					double[] vertex = target[v1 + v2 * data1.length];
					double[] vertex1 = data1[v1];
					double[] vertex2 = data2[v2];

					IndexIterator i1 = reshaped1.getDataDomain().getIterator(left);
					IndexIterator i2 = reshaped2.getDataDomain().getIterator(left);
					for (int l = 0; l < left.getCombinations(); ++l) {
						int offset1 = i1.next();
						int offset2 = i2.next();

						vertex[l] = multiply.applyAsDouble(vertex1[offset1], vertex2[offset2]);
					}
				}
			}
		}

		return builder.get(left, right, target_data);
	}

	@Override
	public String toString() {
		StringBuilder build = new StringBuilder();
		build.append("K(vars").append(Arrays.toString(getDataDomain().getVariables()));
		build.append("|").append(Arrays.toString(getSeparatingDomain().getVariables()));
		build.append(")\n");

		for (int i = 0; i < getDataDomain().getSize(); ++i) {
			build.append(getDataDomain().getVariables()[i]).append(": ");
			IndexIterator iter = getDataDomain().getIterator();
			while (iter.hasNext()) {
				build.append(iter.getPositions()[i]).append(",");
				iter.next();
			}
			build.append("\n");
		}

		return build.toString();
	}

	protected abstract void applyConvexHull();

	@Override
	public VertexAbstractFactor divide(VertexFactor other) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public VertexAbstractFactor marginalize(int variable) {
		return (VertexAbstractFactor) marginalize(new int[]{variable});
	}

	protected <F extends VertexAbstractFactor> F normalize(VertexFactorBuilder<F> builder, int... given) {
		double[][][] newdata = new double[size()][][];
		for (int i = 0; i < size(); ++i) {
			newdata[i] = ArrayUtils.clone(getVerticesAt(i)); // TODO: newdata[i] = data[i].clone()
			for (int v = 0; v < newdata[i].length; ++v) {
				double sum = Arrays.stream(getVerticesAt(i)[v]).sum(); // TODO: double sum = Arrays.stream(data[i][v]).sum();
				newdata[i][v] = Arrays.stream(getVerticesAt(i)[v]).map(x -> x / sum).toArray(); // TODO: newdata[i][v] = Arrays.stream(data[i][v]).map(x -> x / sum).toArray();
			}
		}
		return builder.get(vertexDomain, separatedDomain, newdata);
	}

	public <F extends VertexAbstractFactor> F getSingleVertexFactor(VertexFactorBuilder<F> builder, int... idx) {
		int[] idx_arr;

		if (idx.length == 1) {
			idx_arr = IntStream.range(0, getSeparatingDomain().getCombinations())
					.map(i -> idx[0])
					.toArray();
		} else {
			idx_arr = Arrays.copyOf(idx, idx.length);
		}

		if (idx_arr.length != getSeparatingDomain().getCombinations()) {
			throw new IllegalArgumentException("idx length should be equal to the number combinations of the parents.");
		}

		double[][][] data =
				IntStream.range(0, getSeparatingDomain().getCombinations())
						.mapToObj(i -> new double[][]{this.getVerticesAt(i)[idx_arr[i]]})
						.toArray(double[][][]::new);

		return builder.get(getDataDomain(), getSeparatingDomain(), data);
	}

	@Override
	public BayesianFactor sample() {
		int left_comb = getSeparatingDomain().getCombinations();

		double[] data = Doubles.concat(
				IntStream.range(0, left_comb)
						.mapToObj(i -> this.getVerticesAt(i)[RandomUtil.getRandom().nextInt(getVerticesAt(i).length)])
						.toArray(double[][]::new)
		);

		Strides newDomain = getDataDomain().concat(getSeparatingDomain());
		return new BayesianDefaultFactor(newDomain, data);
	}

}
