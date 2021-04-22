package ch.idsia.crema.factor.credal.linear.interval;

import ch.idsia.crema.core.Strides;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author:  David Huber
 * Project: crema
 * Date:    22.03.2017 18:50
 */
public abstract class IntervalAbstractFactor implements IntervalFactor {

	protected Strides dataDomain;
	protected Strides groupDomain;

	public IntervalAbstractFactor(Strides content, Strides separation) {
		setConditioningDomain(content);
		setDataDomain(separation);
	}

	protected void setDataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
	}

	protected void setConditioningDomain(Strides groupDomain) {
		if (groupDomain == null) {
			groupDomain = new Strides(new int[0], new int[0]);
		}
		this.groupDomain = groupDomain;
	}

	@Override
	public Strides getDomain() {
		return dataDomain.union(groupDomain);
	}

	@Override
	public Strides getDataDomain() {
		return dataDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return groupDomain;
	}

	@Override
	public LinearConstraintSet getLinearProblem(int... states) {
		int offset = groupDomain.getOffset(states);
		return getLinearProblemAt(offset);
	}

	@Override
	public LinearConstraintSet getLinearProblemAt(int offset) {
		ArrayList<LinearConstraint> constraints = new ArrayList<>();
		double[] low = getLowerAt(offset); // TODO: double[] low = lower[offset];
		double[] hig = getUpperAt(offset); // TODO: double[] hig = upper[offset];
		int length = dataDomain.getCombinations();

		for (int i = 0; i < length; ++i) {
			RealVector coeff = new OpenMapRealVector(length);
			coeff.setEntry(i, 1);

			constraints.add(new LinearConstraint(coeff, Relationship.GEQ, low[i]));
			constraints.add(new LinearConstraint(coeff, Relationship.LEQ, hig[i]));
		}

		// and we need a constraint telling us that the sum of the states must be one
		double[] one = new double[length];
		for (int i = 0; i < length; ++i) one[i] = 1;
		constraints.add(new LinearConstraint(one, Relationship.EQ, 1));

		return new LinearConstraintSet(constraints);
	}

	@Override
	public String toString() {
		return "P(" +
				Arrays.toString(getDataDomain().getVariables()) +
				" | " +
				Arrays.toString(getSeparatingDomain().getVariables()) +
				")";
	}

}
