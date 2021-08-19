package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.util.FastMath;

import java.util.function.Function;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    16.04.2021 18:43
 */
// TODO: this class can be moved between AbstractBayesianFactor and BayesianDefaultFactor (f is an accessor to data)
public class BayesianFunctionFactor extends BayesianAbstractFactor {

	protected Function<Integer, Double> f;

	public BayesianFunctionFactor(Strides domain, Function<Integer, Double> f) {
		super(domain);
		this.f = f;
	}

	@Override
	public BayesianFunctionFactor copy() {
		return new BayesianFunctionFactor(domain, f);
	}

	@Override
	public double getValueAt(int index) {
		return f.apply(index);
	}

	@Override
	public double getLogValueAt(int index) {
		return FastMath.log(f.apply(index));
	}

	@Override
	public double[] getData() {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public double logProb(TIntIntMap[] data, int leftVar) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFunctionFactor filter(int variable, int state) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFunctionFactor combine(BayesianFactor other) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFunctionFactor marginalize(int variable) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFunctionFactor divide(BayesianFactor factor) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public ObservationBuilder sample() {
		// TODO
		throw new NotImplementedException();
	}

}
