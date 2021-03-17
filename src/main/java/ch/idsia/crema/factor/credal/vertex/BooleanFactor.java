package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.SeparatelySpecified;
import ch.idsia.crema.model.vertex.VertexOperation;
import ch.idsia.crema.utility.ArraysUtil;

public class BooleanFactor implements SeparatelySpecified<BooleanFactor> {

	private final Strides vertexDomain;
	private final Strides separatedDomain;

	private VertexOperation ops2;

	final private double[][] lower;
	final private double[][] upper;

	public BooleanFactor(Strides vertexDomain, Strides separatedDomain, double[][] nlower, double[][] nupper, VertexOperation ops2) {
		this.lower = nlower;
		this.upper = nupper;
		this.vertexDomain = vertexDomain;
		this.separatedDomain = separatedDomain;
		this.ops2 = ops2;
	}

	@Override
	public BooleanFactor copy() {
		double[][] nlower = ArraysUtil.deepClone(lower);
		double[][] nupper = ArraysUtil.deepClone(upper);
		return new BooleanFactor(vertexDomain, separatedDomain, nlower, nupper, ops2);
	}

	@Override
	public Strides getDomain() {
		return vertexDomain.union(separatedDomain);
	}

	@Override
	public Strides getSeparatingDomain() {
		return separatedDomain;
	}

	@Override
	public Strides getDataDomain() {
		return vertexDomain;
	}

	@Override
	public BooleanFactor filter(int variable, int state) {
		return null;
	}

	public BooleanFactor combineMarginalize(int variable, BooleanFactor... factors) {
		return null;
	}
}
