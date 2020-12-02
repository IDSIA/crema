package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 15:37
 */
public interface JoinInference<F extends GenericFactor, R extends GenericFactor> extends SingleInference<F, R> {

	R apply(GraphicalModel<F> model, int[] query, TIntIntMap observations) throws InterruptedException;

	default R apply(GraphicalModel<F> model, int query, TIntIntMap observations) throws InterruptedException {
		return apply(model, new int[]{query}, observations);
	}

}
