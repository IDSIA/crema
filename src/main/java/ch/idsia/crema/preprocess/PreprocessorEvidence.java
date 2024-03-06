package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 10:49
 * <p>
 * A {@link PreprocessorEvidence} is an algorithm for pre-processing that modifies <u>in place</u> a {@link GraphicalModel}. The
 * given model <u>will</u> be modified without creating a new one.
 */
public interface PreprocessorEvidence<F extends GenericFactor, M extends GraphicalModel<F>> extends Preprocessor<F, M> {

	/**
	 * Perform a pre-processing operation in-place: the given model <u>will</u> be modified.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 */
	void executeInPlace(M model, Int2IntMap evidence);

}
