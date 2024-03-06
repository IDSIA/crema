package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    10.03.2021 10:07
 * <p>
 * This interface is intended to offer the possibility to perform multiple successive queries but with the same
 * pre-processed model. If an inference engine need to not be state-less, this interface can be used to achieve this property.
 */
// TODO find a better name for an inference that can be repeated without re-pre-process the model at each query...
public interface InferenceCascade<M extends GraphicalModel<?>, F extends GenericFactor> {

	/**
	 * Perform an inference.
	 *
	 * @param query the variable that will be queried
	 * @return the result of the inference
	 */
	F query(int query);

	/**
	 * Set the model to use in the next queries. The pre-processing of the model should be done in this method un such
	 * way that multiple future queries (on the same model) will avoid to re-pre-process everything.
	 *
	 * @param model the model to use for inference
	 */
	default void setModel(M model) {
		setModel(model, new Int2IntOpenHashMap());
	}

	/**
	 * Set the model to use in the next queries. The pre-processing of the model should be done in this method un such
	 * way that multiple future queries (on the same model) will avoid to re-pre-process everything.
	 *
	 * @param model    the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 */
	void setModel(M model, Int2IntMap evidence);

	/**
	 * Set the evidence to use in the next queries.
	 *
	 * @param evidence the observed variable as a map of variable-states
	 */
	void setEvidence(Int2IntMap evidence);

}
