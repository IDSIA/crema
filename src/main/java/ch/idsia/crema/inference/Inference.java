package ch.idsia.crema.inference;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Common interface of inference algorithm. Any model preprocessing requirements of the final
 * inference algorithm must be performed byt the query method.
 *
 * @param <M> The model
 * @param <F> The actual Factor type
 * @author davidhuber
 */
public interface Inference<M extends GraphicalModel<?>, F extends GenericFactor> {

	F query(M model, TIntIntMap evidence, int target);

	default F query(M model, int target) {
		return query(model, new TIntIntHashMap(), target);
	}

	// TODO: thinking about separate this in another interface...
	default F query(M model, TIntIntMap evidence, int... targets) {
		// TODO: maybe combine a join there instead of raising an exception?
		throw new UnsupportedOperationException();
	}

	default F query(M model, int... targets) {
		return query(model, new TIntIntHashMap(), targets);
	}
}
