package ch.idsia.crema.factor.credal;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.set.CredalSet;

/**
 * A separately specified credal set where there is a set of something for each instantiation
 * of the parents.
 *
 * @author huber
 */
public class SeparatelySpecifiedCredalFactor<S extends CredalSet> implements SeparatelySpecified<SeparatelySpecifiedCredalFactor<S>> {
	private Strides left, right;

	private CredalSet[] sets;

	public SeparatelySpecifiedCredalFactor() {
		super();
	}

	public SeparatelySpecifiedCredalFactor(Strides dataDomain, Strides groupDomain) {
		left = dataDomain;
		right = groupDomain;
	}

	@Override
	public SeparatelySpecifiedCredalFactor<S> filter(int variable, int state) {
		return null;
	}

	@Override
	public SeparatelySpecifiedCredalFactor<S> copy() {
		SeparatelySpecifiedCredalFactor<S> factor = new SeparatelySpecifiedCredalFactor<>(getDataDomain(), getSeparatingDomain());
		factor.sets = this.sets.clone();
		return factor;
	}

	@Override
	public Strides getSeparatingDomain() {
		return right;
	}

	@Override
	public Strides getDataDomain() {
		return left;
	}

	@Override
	public Strides getDomain() {
		return left.union(right);
	}

}
