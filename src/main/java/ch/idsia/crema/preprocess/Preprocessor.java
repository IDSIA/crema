package ch.idsia.crema.preprocess;

import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 10:49
 * <p>
 * A {@link Preprocessor} is an algorithm for pre-processing that modifies <u>in place</u> a {@link GraphicalModel}. The
 * given model <u>will</u> be modified without creating a new one.
 */
public interface Preprocessor<M extends GraphicalModel<?>> {

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model the model to be processed
	 */
	default void executeInPlace(M model) {
		executeInPlace(model, new TIntIntHashMap(), -1);
	}

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model the model to be processed
	 * @param query the variable that will be queried
	 */
	default void executeInPlace(M model, int query) {
		executeInPlace(model, new TIntIntHashMap(), new int[]{query});
	}

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 */
	default void executeInPlace(M model, TIntIntMap evidence) {
		executeInPlace(model, evidence, -1);
	}

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 */
	default void executeInPlace(M model, TIntIntMap evidence, int query) {
		executeInPlace(model, evidence, new int[]{query});
	}

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model the model to be processed
	 * @param query the variables that will be queried
	 */
	default void executeInPlace(M model, int... query) {
		executeInPlace(model, new TIntIntHashMap(), query);
	}

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variables that will be queried
	 */
	default void executeInPlace(M model, TIntIntMap evidence, int... query) {
		throw new UnsupportedOperationException();
	}

}
