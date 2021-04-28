package ch.idsia.crema.factor.credal.linear.interval;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.ArraysUtil;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.04.2021 18:29
 */
public class IntervalLogFactor extends IntervalDefaultFactor {

	public IntervalLogFactor(Strides content, Strides separation, double[][] lower, double[][] upper) {
		super(content, separation, ArraysUtil.log(lower), ArraysUtil.log(upper));
	}

	public IntervalLogFactor(IntervalDefaultFactor factor) {
		this(factor.dataDomain, factor.groupDomain, ArraysUtil.log(factor.lower), ArraysUtil.log(factor.upper));
	}

	IntervalLogFactor(Strides content, Strides separation, double[][] lower, double[][] upper, boolean isLog) {
		super(content, separation, lower, upper);
	}

	@Override
	public IntervalLogFactor copy() {
		return new IntervalLogFactor(dataDomain, groupDomain, ArraysUtil.deepClone(lower), ArraysUtil.deepClone(upper), true);
	}

	// TODO: complete
}