package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 16:29
 * <p>
 * A {@link TransformerQuery} is an algorithm for pre-processing a {@link GraphicalModel} and produces a new different
 * {@link GraphicalModel} that can be of a complete different type.
 */
public interface TransformerQuery<F extends GenericFactor, M extends GraphicalModel<F>> extends ConverterQuery<F, F, M, M> {

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return a new modified model
	 */
	@Override
	default M execute(M model, Int2IntMap evidence, int query) {
		return execute(model, evidence, new int[]{query});
	}

	/**
	 * Perform a pre-processing operation and return a new model with the modifications.
	 *
	 * @param model    the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variables that will be queried
	 * @return a new modified model
	 */
	@Override
	M execute(M model, Int2IntMap evidence, int... query);

}
