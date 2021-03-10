package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    10.03.2021 10:07
 * <p>
 * This interface is intended to offer the possibility to perform multiple successive queries but with the same
 * pre-processed model. If an inference engine need to not be state-less, this interface can be used to achieve this property.
 * <p>
 * This variant is used for inference engine that supports the inference of multiple query nodes at the same time. It is
 * expected to return a joint probability in a single factor.
 */
// TODO find a better name for an inference that can be repeated without re-pre-process the model at each query...
public interface InferenceCascadeJoined<M extends GraphicalModel<?>, F extends GenericFactor> extends InferenceCascade<M, F> {

	/**
	 * Perform an inference.
	 *
	 * @param query the variables that will be queried
	 * @return the result of the inference
	 */
	F query(int... query);

}
