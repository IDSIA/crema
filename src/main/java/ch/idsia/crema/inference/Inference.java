package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

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
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the result of the inference
	 */
	F query(M model, TIntIntMap evidence, int query);

	/**
	 * Perform an inference.
	 *
	 * @param model the model to be processed
	 * @param query the variable that will be queried
	 * @return the result of the inference
	 */
	default F query(M model, int query) {
		return query(model, new TIntIntHashMap(), query);
	}

	/**
	 * Perform an inference.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @return the result of the inference
	 */
	// TODO: thinking about separate this in another interface...
	default F query(M model, TIntIntMap evidence, int... queries) {
		// TODO: maybe combine a join there instead of raising an exception?
		throw new UnsupportedOperationException();
	}

	/**
	 * Perform an inference.
	 *
	 * @param model the model to be processed
	 * @return the result of the inference
	 */
	default F query(M model, int... queries) {
		return query(model, new TIntIntHashMap(), queries);
	}
}
