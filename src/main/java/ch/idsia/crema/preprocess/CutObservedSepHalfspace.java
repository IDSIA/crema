package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;

/**
 * A Network alteration step that cuts outbound arcs of observed nodes and
 * filter the children's factors.
 *
 * @author rcabanas
 */
public class CutObservedSepHalfspace {

	/**
	 * Execute the operation on the provided network.
	 * You should not use the inplace method! it is bad!
	 *
	 * @param model    the model to be preprocessed
	 * @param evidence a collection of instantiations containing variable - state
	 *                 pairs
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void executeInplace(final GraphicalModel model, final TIntIntMap evidence) {
		final int size = evidence.size();

		final TIntIntIterator iterator = evidence.iterator();
		for (int o = 0; o < size; ++o) {
			iterator.advance();
			final int observed = iterator.key();
			final int state = iterator.value();

			//int[] affected = Ints.concat(model.getChildren(observed), new int[]{observed});

			final int[] affected = model.getChildren(observed);

			for (final int variable : affected) {
				final SeparateHalfspaceFactor new_factor = ((SeparateHalfspaceFactor) model.getFactor(variable))
                        .filter(observed, state);
				if (variable != observed)
				    model.removeParent(variable, observed);
				model.setFactor(variable, new_factor);

			}
		}
	}

	/**
	 * Execute the algorithm and return the modified NEW network. The original
	 * network is unchanged!
	 *
	 * @param model    the model to be preprocessed
	 * @param evidence a collection of instantiations containing variable - state
	 *                 pairs
	 */
	@SuppressWarnings({"rawtypes"})
	public GraphicalModel execute(final GraphicalModel model, final TIntIntMap evidence) {

		final GraphicalModel copy = model.copy();
		executeInplace(copy, evidence);
		return copy;
	}
}
