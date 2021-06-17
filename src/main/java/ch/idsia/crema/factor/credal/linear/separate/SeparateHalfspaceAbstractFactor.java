package ch.idsia.crema.factor.credal.linear.separate;

import ch.idsia.crema.core.Strides;

/**
 * An abstract implementation of the {@link SeparateHalfspaceAbstractFactor} interface.
 *
 * @param <F> the managed type. This is usually the type itself.
 * @author david
 */
public abstract class SeparateHalfspaceAbstractFactor implements SeparateHalfspaceFactor {

	protected Strides dataDomain = Strides.empty();
	protected Strides groupDomain = Strides.empty();

	public SeparateHalfspaceAbstractFactor() {
	}

	public SeparateHalfspaceAbstractFactor(Strides dataDomain, Strides groupDomain) {
		setConditioningDomain(groupDomain);
		setDataDomain(dataDomain);
	}

	private void setDataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
	}

	private void setConditioningDomain(Strides groupDomain) {
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
