package ch.idsia.crema.factor.credal.linear.interval;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysMath;
import ch.idsia.crema.utility.ArraysUtil;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.04.2021 18:29
 */
public class IntervalLogFactor extends IntervalDefaultFactor {

	public IntervalLogFactor(Strides content, Strides separation, double[][] lower, double[][] upper) {
		super(content, separation, ArraysMath.log(lower), ArraysMath.log(upper));
	}

	public IntervalLogFactor(IntervalDefaultFactor factor) {
		this(factor.dataDomain, factor.groupDomain, ArraysMath.log(factor.lower), ArraysMath.log(factor.upper));
	}

	IntervalLogFactor(Strides content, Strides separation, double[][] lower, double[][] upper, boolean isLog) {
		super(content, separation, lower, upper);
	}

	@Override
	public IntervalLogFactor copy() {
		return new IntervalLogFactor(dataDomain, groupDomain, ArraysUtil.deepClone(lower), ArraysUtil.deepClone(upper), true);
	}

	@Override
	public double[] getLower(int... states) {
		return ArraysMath.exp(getLogLower(states));
	}

	@Override
	public double[] getUpper(int... states) {
		return ArraysMath.exp(getLogUpper(states));
	}

	@Override
	public double[] getLowerAt(int group_offset) {
		return ArraysMath.exp(getLogLowerAt(group_offset));
	}

	@Override
	public double[] getUpperAt(int group_offset) {
		return ArraysMath.exp(getLogUpperAt(group_offset));
	}

	@Override
	public double[] getLogLower(int... states) {
		return lower[groupDomain.getOffset(states)];
	}

	@Override
	public double[] getLogUpper(int... states) {
		return upper[groupDomain.getOffset(states)];
	}

	@Override
	public double[] getLogLowerAt(int group_offset) {
		return lower[group_offset];
	}

	@Override
	public double[] getLogUpperAt(int group_offset) {
		return upper[group_offset];
	}

	public IntervalDefaultFactor exp() {
		return new IntervalDefaultFactor(dataDomain, groupDomain, ArraysMath.exp(lower), ArraysMath.exp(upper));
	}

	@Override
	public IntervalLogFactor filter(int variable, int state) {
		final IntervalDefaultFactor f = super.filter(variable, state);
		return new IntervalLogFactor(f.dataDomain, f.groupDomain, f.lower, f.upper, true);
	}

	@Override
	public IntervalLogFactor updateReachability() {
		return new IntervalLogFactor(exp().updateReachability());
	}

	@Override
	public IntervalDefaultFactor merge(IntervalFactor factor) {
		final IntervalDefaultFactor f = super.merge(factor);
		return new IntervalLogFactor(f.dataDomain, f.groupDomain, f.lower, f.upper, true);
	}

}