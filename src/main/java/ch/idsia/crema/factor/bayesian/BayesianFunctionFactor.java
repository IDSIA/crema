package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.IndexIterator;
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
public abstract class BayesianFunctionFactor extends BayesianAbstractFactor {

	protected Function<Integer, Double> f;

	public BayesianFunctionFactor(Strides domain, Function<Integer, Double> f) {
		super(domain);
		this.f = f;
	}

	protected BayesianFunctionFactor(Strides domain) {
		super(domain);
	}

	protected void setF(Function<Integer, Double> f) {
		this.f = f;
	}

	@Override
	public double getValue(int... states) {
		return getValueAt(getDomain().getOffset(states));
	}

	@Override
	public double getLogValue(int... states) {
		return getLogValueAt(getDomain().getOffset(states));
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
		// TODO: do we really want to allow the generation of the FULL table of all possible values?
		final IndexIterator it = getDomain().getIterator();
		final int size = getDomain().getCombinations();

		final double[] v = new double[size];
		for (int i = 0; i < size; i++) {
			v[i] = getValueAt(it.next());
		}

		return v;
	}

	@Override
	public double logProb(TIntIntMap[] data, int leftVar) {
		// TODO
		throw new NotImplementedException();
	}

}
