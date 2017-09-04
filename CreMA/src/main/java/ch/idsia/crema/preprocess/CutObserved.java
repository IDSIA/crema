package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.Instantiation;
import ch.idsia.crema.model.change.NullChange;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * A Network alteration step that cuts outbound arcs of observed nodes and
 * filter the children's factors.
 *
 * FIXME Uniform evidence representation!!!!!
 * @author huber
 *
 */
public class CutObserved {

	/**
	 * Execute the operation on the provided network. 
	 * You should not use the inplace method! it is bad!
	 * 
	 * @param model
	 *            the model to be preprocessed
	 * @param evidence
	 *            a collection of instantiations containing variable - state
	 *            pairs
	 */
	public <F extends Factor<F>> void executeInplace(GraphicalModel<F> model, Collection<Instantiation> evidence) {
		int size = evidence.size();

		Iterator<Instantiation> iterator = evidence.iterator();
		for (int o = 0; o < size; ++o) {
			Instantiation item = iterator.next();
			final int observed = item.getVariable();
			final int state = item.getState();

			for (int variable : model.getChildren(observed)) {
				model.removeParent(variable, observed, new NullChange<F>() {
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
	 * Execute the algorithm and return the modified NEW network. The original
	 * network is unchanged!
	 * 
	 * @param model
	 *            the model to be preprocessed
	 * @param query
	 *            the query nodes
	 * @param evidence
	 *            a collection of instantiations containing variable - state
	 *            pairs
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <F extends GraphicalModel> F execute(F model, Collection<Instantiation> evidence) {

		F copy = (F) model.copy();
		executeInplace(copy, evidence);
		return copy;
	}


	/**
	 * Execute the operation on the provided network.
	 * You should not use the inplace method! it is bad!
	 *
	 * @param model
	 *            the model to be preprocessed
	 * @param evidence
	 *            a collection of instantiations containing variable - state
	 *            pairs
	 */
	public <F extends FilterableFactor<F>> void executeInplace(GraphicalModel<F> model, TIntIntMap evidence) {
		int size = evidence.size();

		TIntIntIterator iterator = evidence.iterator();
		for (int o = 0; o < size; ++o) {
			iterator.advance();
			final int observed = iterator.key();
			final int state = iterator.value();

			for (int variable : model.getChildren(observed)) {
				model.removeParent(variable, observed, new NullChange<F>() {
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
	 * Execute the algorithm and return the modified NEW network. The original
	 * network is unchanged!
	 *
	 * @param model
	 *            the model to be preprocessed
	 * @param query
	 *            the query nodes
	 * @param evidence
	 *            a collection of instantiations containing variable - state
	 *            pairs
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <F extends GraphicalModel> F execute(F model, TIntIntMap evidence) {

		F copy = (F) model.copy();
		executeInplace(copy, evidence);
		return copy;
	}
}
