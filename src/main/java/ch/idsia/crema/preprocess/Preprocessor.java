package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 10:49
 * <p>
 * A {@link Preprocessor} is an algorithm for pre-processing that modifies <u>in place</u> a {@link GraphicalModel}. The
 * given model <u>will</u> be modified without creating a new one.
 */
public interface Preprocessor<F extends GenericFactor, M extends GraphicalModel<F>> {

}
