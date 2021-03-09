package ch.idsia.crema.preprocess;

import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 16:29
 * <p>
 * A {@link Converter} is an algorithm for pre-processing a {@link GraphicalModel} and produces a new different
 * {@link GraphicalModel} that can be of a complete different type.
 */
@SuppressWarnings("unchecked")
public interface Converter<IN extends GraphicalModel<?>, OUT extends GraphicalModel<?>> extends Preprocessor<IN> {

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model the model to be processed
	 * @return a new modified model
	 */
	default OUT execute(IN model) {
		return execute(model, new TIntIntHashMap(), -1);
	}

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model the model to be processed
	 * @param query the variable that will be queried
	 * @return a new modified model
	 */
	default OUT execute(IN model, int query) {
		return execute(model, new TIntIntHashMap(), new int[]{query});
	}

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @return a new modified model
	 */
	default OUT execute(IN model, TIntIntMap evidence) {
		return execute(model, evidence, -1);
	}

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return a new modified model
	 */
	default OUT execute(IN model, TIntIntMap evidence, int query) {
		return execute(model, evidence, new int[]{query});
	}

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model the model to be processed
	 * @param query the variables that will be queried
	 * @return a new modified model
	 */
	default OUT execute(IN model, int... query) {
		return execute(model, new TIntIntHashMap(), query);
	}

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variables that will be queried
	 * @return a new modified model
	 */
	default OUT execute(IN model, TIntIntMap evidence, int... query) {
		IN copy = (IN) model.copy();
		executeInPlace(copy, evidence, query);
		return (OUT) copy;
	}

}
