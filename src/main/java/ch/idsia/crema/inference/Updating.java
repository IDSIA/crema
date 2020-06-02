package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.GraphicalModel;
import gnu.trove.map.TIntIntMap;

import java.util.Collection;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 14:29
 */
public interface Updating<F extends GenericFactor, R extends GenericFactor> {

	Collection<R> apply(GraphicalModel<F> model, int[] query) throws InterruptedException;

	Collection<R> apply(GraphicalModel<F> model, int[] query, TIntIntMap observations) throws InterruptedException;

	default Collection<R> apply(GraphicalModel<F> model, int query, TIntIntMap observations) throws InterruptedException {
		return apply(model, new int[]{query}, observations);
	}

}
