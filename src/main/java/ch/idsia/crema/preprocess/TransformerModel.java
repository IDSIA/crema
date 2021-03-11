package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 16:29
 * <p>
 * A {@link TransformerModel} is an algorithm for pre-processing a {@link GraphicalModel} and produces a new different
 * {@link GraphicalModel} that can be of a complete different type.
 */
public interface TransformerModel<F extends GenericFactor, M extends GraphicalModel<F>> extends ConverterModel<F, F, M, M> {

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model the model to be processed
	 * @return a new modified model
	 */
	@Override
	M execute(M model);

}
