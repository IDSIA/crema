package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/**
 * Common interface of inference algorithm. Any model preprocessing requirements of the final
 * inference algorithm must be performed byt the query method.
 * <p>
 * This variant is used for inference engine that supports the inference of multiple query nodes at the same time. It is
 * expected to return a joint probability in a single factor.
 *
 * @param <M> The model
 * @param <F> The actual Factor type
 * @author davidhuber
 */
public interface InferenceJoined<M extends GraphicalModel<?>, F extends GenericFactor> extends Inference<M, F> {

	/**
	 * Perform an inference.
	 *
	 * @param model    the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @return the result of the inference
	 */
	F query(M model, Int2IntMap evidence, int... queries);

	/**
	 * Perform an inference.
	 *
	 * @param model the model to use for inference
	 * @return the result of the inference
	 */
	default F query(M model, int... queries) {
		return query(model, new Int2IntOpenHashMap(), queries);
	}

}
