package ch.idsia.crema.preprocess;

import ch.idsia.crema.model.graphical.GraphicalModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    09.03.2021 16:29
 * <p>
 * A {@link Transformer} is a particular case of a {@link Converter} where the output and input models are of the same
 * type.
 */
public interface Transformer<M extends GraphicalModel<?>> extends Converter<M, M> {

}
