package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/**
 * Common interface of inference algorithm. Any model preprocessing requirements of the final
 * inference algorithm must be performed byt the query method.
 *
 * @param <M> The model
 * @param <F> The actual Factor type
 * @author davidhuber
 */
public interface Inference<M extends GraphicalModel<?>, F extends GenericFactor> {

	/**
	 * Perform an inference.
	 *
	 * @param model    the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the result of the inference
	 */
	F query(M model, Int2IntMap evidence, int query);

	/**
	 * Perform an inference.
	 *
	 * @param model the model to use for inference
	 * @param query the variable that will be queried
	 * @return the result of the inference
	 */
	default F query(M model, int query) {
		return query(model, new Int2IntOpenHashMap(), query);
	}

}
