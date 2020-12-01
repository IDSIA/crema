package ch.idsia.crema.factor.credal.set;

import ch.idsia.crema.core.Strides;

public abstract class AbstractSet implements CredalSet {

	protected Strides domain;

	public AbstractSet() {
	}

	public AbstractSet(Strides domain) {
		this.domain = domain;
	}

	public void setDomain(Strides domain) {
		this.domain = domain;
	}

	@Override
	public Strides getDomain() {
		return domain;
	}
}
