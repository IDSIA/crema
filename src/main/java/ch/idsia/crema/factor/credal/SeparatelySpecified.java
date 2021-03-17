package ch.idsia.crema.factor.credal;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.GenericFilterableFactor;
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
public interface SeparatelySpecified<F extends SeparatelySpecified<F>> extends GenericFilterableFactor<F> {

	/**
	 * The domain of the separated part. This is usually the conditioning of the Factor.
	 * This side must not overlap with the data domain returned by {@link #getDataDomain()}.
	 *
	 * @return
	 */
	Strides getSeparatingDomain();

	/**
	 * The domain of the rest of the domain.
	 * Together with {@link #getSeparatingDomain()} it will form the complete domain
	 * of the object.
	 *
	 * @return
	 */
	Strides getDataDomain();

	/**
	 * Sorts the parents following the global variable order
	 *
	 * @return
	 */
	default F sortParents() {
		throw new NotImplementedException("sortParents not implemented");
	}

}
