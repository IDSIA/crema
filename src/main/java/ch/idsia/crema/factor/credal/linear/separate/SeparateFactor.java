package ch.idsia.crema.factor.credal.linear.separate;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.SeparatelySpecified;

/**
 * An abstract implementation of the {@link SeparateFactor} interface.
 *
 * @param <F> the managed type. This is usually the type itself.
 * @author david
 */
public abstract class SeparateFactor<F extends SeparateFactor<F>> implements SeparatelySpecified<F> {

	protected Strides dataDomain;

	protected Strides groupDomain;

	public SeparateFactor() {
	}

	public SeparateFactor(Strides dataDomain, Strides groupDomain) {
		setConditioningDomain(groupDomain);
		setDataDomain(dataDomain);
	}

	public void setDataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
	}

	public void setConditioningDomain(Strides groupDomain) {
		if (groupDomain == null) {
			groupDomain = new Strides(new int[0], new int[0]);
		}
		this.groupDomain = groupDomain;
	}

	@Override
	public Strides getDataDomain() {
		return dataDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return groupDomain;
	}

	@Override
	public Strides getDomain() {
		return dataDomain.union(groupDomain);
	}

}
