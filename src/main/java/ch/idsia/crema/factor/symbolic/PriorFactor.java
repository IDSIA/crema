package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.factor.GenericFactor;

public class PriorFactor extends SymbolicFactor {
	private GenericFactor factor;
	
	public PriorFactor(GenericFactor lf) {
		super(lf.getDomain());
		this.factor = lf;
	}
	
	public GenericFactor getFactor() {
		return factor;
	}
}
