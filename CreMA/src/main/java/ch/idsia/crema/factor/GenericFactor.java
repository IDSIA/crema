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
	 * @return
	 */
	public GenericFactor copy();
	
	/**
	 * The domain of the factor. This includes variables, sizes and strides.
	 * @return
	 */
	public Strides getDomain();
}
