package ch.idsia.crema.factor;

import ch.idsia.crema.model.Strides;

/**
 * A factor. Please keep them unmutable.
 *
 * @author davidhuber
 */
public interface GenericFactor {
	/**
	 * Make a copy of the factor
	 *
	 * @return
	 */
	GenericFactor copy();

	/**
	 * The domain of the factor. This includes variables, sizes and strides.
	 *
	 * @return
	 */
	Strides getDomain();

	default GenericFactor getDeterministic(int var, int state) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Replaces the IDs of the variables in the domain
	 *
	 * @param new_vars
	 * @return
	 */
	default GenericFactor renameDomain(int... new_vars) {
		throw new java.lang.UnsupportedOperationException("Not supported yet.");
	}

}
