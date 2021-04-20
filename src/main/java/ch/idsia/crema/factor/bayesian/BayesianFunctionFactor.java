package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.NotImplementedException;

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
	public BayesianFactor replace(double value, double replacement) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFactor replaceNaN(double replacement) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public void sortDomain() {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFactor addition(BayesianFactor factor) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public double logProb(TIntIntMap[] data, int leftVar) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public void replaceInplace(double value, double replacement) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFactor filter(int variable, int state) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFunctionFactor combine(BayesianFactor other) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFactor marginalize(int variable) {
		// TODO
		throw new NotImplementedException();
	}

	@Override
	public BayesianFactor divide(BayesianFactor factor) {
		// TODO
		throw new NotImplementedException();
	}

}
