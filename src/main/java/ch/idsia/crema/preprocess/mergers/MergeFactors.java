package ch.idsia.crema.preprocess.mergers;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.04.2021 13:59
 */
public interface MergeFactors<F extends GenericFactor> {

	F merge(GraphicalModel<F> model, Int2IntMap evidence, int x1, int x2, int x, int y);

}
