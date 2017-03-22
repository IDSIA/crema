package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    22.03.2017 15:38
 */
public interface SingleInference<F extends GenericFactor, R extends GenericFactor> {

    R apply(GraphicalModel<F> model, int query, TIntIntMap observations) throws InterruptedException;

    default R apply(GraphicalModel<F> model, int query) throws InterruptedException {
        return apply(model, query, new TIntIntHashMap());
    }

}
