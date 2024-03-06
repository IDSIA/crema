package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.model.change.NullChange;
import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

/**
 * A Network alteration step that cuts outbound arcs of observed nodes and filter the children's factors.
 *
 * @author huber
 */
public class Observe<F extends FilterableFactor<F>> implements TransformerEvidence<F, GraphicalModel<F>>,
		PreprocessorEvidence<F, GraphicalModel<F>> {

	/**
	 * Execute the operation on the provided network.
	 * You should not use the inplace method! it is bad!
	 *
	 * @param model    the model to be preprocessed
	 * @param evidence a collection of instantiations containing variable - state
	 *                 pairs
	 */
	// TODO: remove this method in favor of #execute (below)
	@Override
	public void executeInPlace(GraphicalModel<F> model, Int2IntMap evidence) {
		final int size = evidence.size();

		final var iterator = evidence.int2IntEntrySet().iterator();
		for (int o = 0; o < size; ++o) {
			var entry = iterator.next();
			final int observed = entry.getIntKey();
			final int state = entry.getIntValue();

			for (int variable : model.getChildren(observed)) {
				model.removeParent(variable, observed, (factor) ->  {
					// probably need to check this earlier
					return factor.filter(observed, state);
				});
			}
		}
	}

	/**
	 * Execute the algorithm and return the modified NEW network.
	 * The original network is unchanged!
	 *
	 * @param model    the model to be preprocessed
	 * @param evidence a collection of instantiations containing variable - state
	 *                 pairs
	 */
	@Override
	public GraphicalModel<F> execute(GraphicalModel<F> model, Int2IntMap evidence) {
		GraphicalModel<F> copy = model.copy();
		executeInPlace(copy, evidence);
		return copy;
	}
}
