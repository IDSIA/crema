package ch.idsia.crema.factor.credal;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.SeparateFactor;
import ch.idsia.crema.model.Strides;

/**
 * An abstract implementation of the {@link SeparateFactor} interface.
 * 
 * @author david
 * @param <F>
 *            the managed type. This is usually the type itself.
 */
public abstract class ConditionalFactor<F extends ConditionalFactor<F>> implements GenericFactor, SeparatelySpecified<F> {

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
