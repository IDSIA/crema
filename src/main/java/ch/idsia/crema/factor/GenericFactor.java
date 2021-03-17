package ch.idsia.crema.factor;

import ch.idsia.crema.core.Strides;

/**
 * A factor. Please keep them unmutable.
 *
 * @author davidhuber
 */
public interface GenericFactor {
	/**
	 * @return a copy of the factor
	 */
	GenericFactor copy();

	/**
	 * @return The domain of the factor. This includes variables, sizes and strides.
	 */
	Strides getDomain();

	/**
	 * Replaces the IDs of the variables in the domain.
	 *
	 * @param new_vars new id to use
	 * @return
	 */
	default GenericFactor renameDomain(int... new_vars) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	default GenericFactor getDeterministic(int var, int state) {
		throw new UnsupportedOperationException();
	}

}
