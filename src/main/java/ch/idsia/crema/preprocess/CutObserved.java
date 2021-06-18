package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.model.change.NullChange;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;

/**
 * A Network alteration step that cuts outbound arcs of observed nodes and filter the children's factors.
 *
 * @author huber
 */
public class CutObserved<F extends FilterableFactor<F>> implements TransformerEvidence<F, GraphicalModel<F>>,
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
	public void executeInPlace(GraphicalModel<F> model, TIntIntMap evidence) {
		final int size = evidence.size();

		final TIntIntIterator iterator = evidence.iterator();
		for (int o = 0; o < size; ++o) {
			iterator.advance();
			final int observed = iterator.key();
			final int state = iterator.value();

			for (int variable : model.getChildren(observed)) {
				model.removeParent(variable, observed, new NullChange<>() {
					@Override
					public F remove(F factor, int variable) {
						// probably need to check this earlier
						return factor.filter(observed, state);
					}
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
	public GraphicalModel<F> execute(GraphicalModel<F> model, TIntIntMap evidence) {
		GraphicalModel<F> copy = model.copy();
		executeInPlace(copy, evidence);
		return copy;
	}
}
