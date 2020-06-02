package ch.idsia.crema.factor.credal.set;

import ch.idsia.crema.model.Domain;

public interface CredalSet {
	Domain getDomain();
	CredalSet copy();
}
