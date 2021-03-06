package ch.idsia.crema.factor.credal.vertex.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorFactory;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.hull.ConvexHull;
import com.google.common.primitives.Doubles;
import gnu.trove.list.TIntList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 21:45
 */
public class VertexDefaultFactor extends VertexAbstractFactor {

	protected final double[][][] data;

	public VertexDefaultFactor(Strides vertexDomain, Strides separatedDomain, double[][][] data) {
		super(vertexDomain, separatedDomain);
		this.data = data;
	}

	public VertexDefaultFactor(Strides vertexDomain, Strides separatedDomain, List<double[]> vertices, TIntList combinations) {
		super(vertexDomain, separatedDomain);
		final int n = separatedDomain.getCombinations();
		data = new double[n][][];

		for (int i = 0; i < combinations.size(); i++) {
			addVertex(vertices.get(i), combinations.get(i));
		}
	}

	public VertexDefaultFactor(Strides vertexDomain, double[][] coefficients, double[] values, Relationship... rel) {
		super(vertexDomain, Strides.empty());

		// check the coefficient sizes
		for (double[] c : coefficients) {
			if (c.length != vertexDomain.getCombinations())
				throw new IllegalArgumentException("Wrong constraint size: " + c.length + " instead of " + vertexDomain.getCombinations());
		}

		// check the relationship vector length
		if (rel.length == 0) rel = new Relationship[]{Relationship.EQ};
		if (rel.length == 1) {
			Relationship[] rel_aux = new Relationship[coefficients.length];
			for (int i = 0; i < coefficients.length; i++)
				rel_aux[i] = rel[0];
			rel = rel_aux;
		} else if (rel.length != coefficients.length) {
			throw new IllegalArgumentException("Wrong relationship vector length: " + rel.length);
		}

		SeparateHalfspaceFactorFactory shff = SeparateHalfspaceFactorFactory.factory().domain(vertexDomain, Strides.empty());
		for (int i = 0; i < coefficients.length; i++) {
			shff.constraint(coefficients[i], rel[i], values[i]);
		}

		// normalization constraint
		double[] ones = new double[vertexDomain.getCombinations()];
		Arrays.fill(ones, 1.);
		shff.constraint(ones, Relationship.EQ, 1.0);

		// non-negative constraints
		double[] zeros = new double[vertexDomain.getCombinations()];
		for (int i = 0; i < zeros.length; i++)
			ones[i] = 0.;

		for (int i = 0; i < vertexDomain.getCombinations(); i++) {
			double[] c = ArrayUtils.clone(zeros);
			c[i] = 1.;
			shff.constraint(c, Relationship.GEQ, 0);
		}

		SeparateHalfspaceFactor k_const = shff.get();

		// HalfspaceToVertex returns always a VertexDefaultFactor
		HalfspaceToVertex conversor = new HalfspaceToVertex();
		double[][] vertices = ((VertexDefaultFactor) conversor.apply(k_const, 0)).data[0];

		if (vertices == null || vertices.length == 0) {
			throw new NoFeasibleSolutionException();
		}

		this.data = new double[vertices.length][][];

		//add extreme points
		for (double[] v : vertices) {
			this.addVertex(v);
		}
	}

	public VertexDefaultFactor(SeparateHalfspaceFactor constrainsFactor) {
		super(constrainsFactor.getDataDomain(), constrainsFactor.getSeparatingDomain());

		data = new double[1][][];

		// HalfspaceToVertex returns always a VertexDefaultFactor
		HalfspaceToVertex conversor = new HalfspaceToVertex();
		double[][] vertices = ((VertexDefaultFactor) conversor.apply(constrainsFactor, 0)).data[0];

		if (vertices == null || vertices.length == 0) {
			throw new NoFeasibleSolutionException();
		}
		//add extreme points
		for (double[] v : vertices) {
			addVertex(v);
		}
	}

	@Override
	public VertexDefaultFactor copy() {
		double[][][] copy = ArraysUtil.deepClone(data);
		return new VertexDefaultFactor(vertexDomain, separatedDomain, copy);
	}

	@Override
	public int size() {
		return data.length;
	}

	protected void addVertex(double[] vertex, int... groupStates) {
		int offset = separatedDomain.getOffset(groupStates);
		double[][] gdata = data[offset];

		if (gdata != null) { // not first vertex
			int len = gdata.length;
			double[][] newdata = Arrays.copyOf(gdata, len + 1);
			newdata[len] = vertex;
			data[offset] = newdata;
		} else {
			data[offset] = new double[][]{vertex};
		}
	}

	@Override
	public double[][] getVertices(int... states) {
		final int offset = separatedDomain.getOffset(states);
		final double[][] arr = data[offset];
		if (arr == null)
			return null;
		return ArraysUtil.deepClone(arr); // TODO: check if pollute memory or too slow
	}

	@Override
	public double[][][] getData() {
		return ArraysUtil.deepClone(data);
	}

	@Override
	public VertexDefaultFactor filter(int variable, int state) {
		return filter(variable, state, VertexDefaultFactor::new);
	}

	@Override
	public VertexDefaultFactor marginalize(int... vars) {
		return marginalize(VertexDefaultFactor::new, vars);
	}

	@Override
	public VertexDefaultFactor reseparate(Strides target) {
		return reseparate(target, VertexDefaultFactor::new);
	}

	@Override
	public VertexDefaultFactor combine(VertexFactor other) {
		return combine(other, VertexDefaultFactor::new, (a, b) -> a * b);
	}

	@Override
	public VertexDefaultFactor normalize(int... given) {
		return normalize(VertexDefaultFactor::new, given);
	}

	@Override
	public VertexDefaultFactor getSingleVertexFactor(int... idx) {
		return getSingleVertexFactor(VertexDefaultFactor::new, idx);
	}

	@Override
	public String toString() {
		StringBuilder build = new StringBuilder(super.toString());

		IndexIterator condition = separatedDomain.getIterator();
		int[] c = condition.getPositions();
		int cp = 0;

		while (condition.hasNext()) {
			String head = "K(vars" + Arrays.toString(getDataDomain().getVariables()) + "|" + Arrays.toString(c) + ") ";
			String white = IntStream.range(0, head.length()).mapToObj(x -> " ").collect(Collectors.joining());
			for (int v = 0; v < data[cp].length; ++v) {
				build.append(head);
				build.append(Arrays.toString(data[cp][v]));
				build.append("\n");
				head = white;
			}
			condition.next();
			cp++;
		}

		return build.toString();
	}

	public Iterator<double[][]> getVertexSetIterator() {
		return Arrays.asList(data).iterator();
	}

	@Override
	public double[][] getVerticesAt(int i) {
		return data[i];
	}

	@Override
	protected void applyConvexHull(ConvexHull m) {
		if (m != null) {
			for (int i = 0; i < this.getSeparatingDomain().getCombinations(); i++) {
				data[i] = m.apply(data[i]);
			}
		}
	}

	@Override
	public VertexDefaultFactor convexHull(ConvexHull m) {
		VertexDefaultFactor f = this.copy();
		f.applyConvexHull(m);
		return f;
	}

	@Override
	public VertexDefaultFactor merge(VertexFactor f) {
		double[][][] vertices = new double[f.getSeparatingDomain().getCombinations()][][];
		for (int i = 0; i < f.getSeparatingDomain().getCombinations(); i++) {
			double[][] v1 = this.getVerticesAt(i);
			double[][] v2 = f.getVerticesAt(i);
			vertices[i] = ArraysUtil.reshape2d(
					Doubles.concat(Doubles.concat(v1), Doubles.concat(v2)),
					v1.length + v2.length);
		}
		return new VertexDefaultFactor(f.getDataDomain(), f.getSeparatingDomain(), vertices);
	}

}
