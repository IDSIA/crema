package ch.idsia.crema.factor.credal;

import ch.idsia.crema.core.Strides;

/**
 * An abstract implementation of the {@link SeparatelySpecified} interface.
 *
 * @param <F> the managed type. This is usually the type itself.
 * @author david
 */
public abstract class ConditionalFactor<F extends ConditionalFactor<F>> implements SeparatelySpecified<F> {

	protected Strides dataDomain;
	protected Strides conditioningDomain;

	public ConditionalFactor(Strides dataDomain, Strides conditioningDomain) {
		if (conditioningDomain == null) {
			conditioningDomain = new Strides(new int[0], new int[0]);
		}

		this.dataDomain = dataDomain;
		this.conditioningDomain = conditioningDomain;
	}

	@Override
	public Strides getDataDomain() {
		return dataDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return conditioningDomain;
	}

	@Override
	public Strides getDomain() {
		return dataDomain.union(conditioningDomain);
	}

	@Override
	public abstract F copy();
}
