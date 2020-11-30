package ch.idsia.crema.factor.credal;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import org.apache.commons.lang3.NotImplementedException;

/**
 * A separately specified Factor has a for each possible 
 * combination of its "grouping" domain. Usually this domain corresponds with 
 * the conditioning domain. The remaining part of the factor's domain is the Data domain. Grouping and Data
 * domain do not overlap and, when combined must be equal to the complete domain 
 * obtained by {@link GenericFactor#getDomain()}.</p>
 * 
 * @author david
 */
public interface SeparatelySpecified<F extends SeparatelySpecified<F>> {
	
	/**
	 * The domain of the separated part. This is usually the conditioning of the Factor. 
	 * This side must not overlap with the data domain returned by {@link #getDataDomain()}.
	 * 
	 * @return
	 */
	public Strides getSeparatingDomain();
	
	/**
	 * The domain of the rest of the domain. 
	 * Together with {@link #getSeparatingDomain()} it will form the complete domain
	 * of the object. 
	 * 
	 * @return
	 */
	public Strides getDataDomain();
	
	/**
	 * A separately specified factor must be able to filter out given 
	 * variables on the grouping side of the factor.
	 * 
	 * @param variable
	 * @param state
	 * @return
	 */
	public F filter(int variable, int state);


	/**
	 * Sorts the parents following the global variable order
	 * @return
	 */
	public default F sortParents(){
		throw new NotImplementedException("sortParents not implemented");
	}
	
}
