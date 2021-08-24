package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.LogSpace;
import org.apache.commons.math3.util.FastMath;

import java.util.function.Function;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.08.2021 10:01
 */
@LogSpace
public abstract class BayesianFunctionLogFactor extends BayesianFunctionFactor {

	public BayesianFunctionLogFactor(Strides domain, Function<Integer, Double> f) {
		super(domain, f);
	}

	@Override
	public double getValueAt(int index) {
		return FastMath.exp(f.apply(index));
	}

	@Override
	public double getLogValueAt(int index) {
		return f.apply(index);
	}

}
