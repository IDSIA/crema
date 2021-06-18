package ch.idsia.crema.factor.credal.linear.interval;

import ch.idsia.crema.core.Strides;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 09:31
 */
public class IntervalVacuousFactor extends IntervalDefaultFactor {

	public IntervalVacuousFactor(Strides content, Strides separation) {
		super(content, separation);

		// init all to 0-1
		for (int i = 0; i < groupDomain.getCombinations(); ++i) {
			Arrays.fill(this.upper[i], 1.0);
			//Arrays.fill(this.lower[i], 0.0); // is 0 anyway
		}
	}

}
