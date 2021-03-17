package ch.idsia.crema.factor.credal;

import ch.idsia.crema.factor.Factor;

/**
 * A marker interface for credal Factors
 *
 * @author david
 */
public interface CredalFactor<F extends Factor<F> & SeparatelySpecified<F>> extends SeparatelySpecified<F>, Factor<F> {

}
