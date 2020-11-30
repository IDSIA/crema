package ch.idsia.crema.factor.credal.set;

import ch.idsia.crema.core.Domain;

public interface CredalSet {
	Domain getDomain();
	CredalSet copy();
}
