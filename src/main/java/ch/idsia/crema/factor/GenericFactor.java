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

}
