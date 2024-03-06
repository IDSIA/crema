package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.search.SearchOperation;
import ch.idsia.crema.search.impl.DepthFirst;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class RemoveBarren<F extends GenericFactor> implements PreprocessorQuery<F, GraphicalModel<F>>, TransformerQuery<F, GraphicalModel<F>> {

	private IntList deleted;

	/**
	 * @return Returns a copy of the array with the deleted variables.
	 */
	public int[] getDeleted() {
		return deleted.toIntArray();
	}

	/**
	 * Remove barren variable from the specified model.
	 *
	 * @param model    the model to be processed
	 * @param query    the variable that will be queried
	 * @param evidence the observed variable as a map of variable-states
	 */
	@Override
	public void executeInPlace(GraphicalModel<F> model, Int2IntMap evidence, int... query) {
		IntSet retain = cutIndependent(model, query, evidence.keySet());
		deleted = new IntArrayList();

		for (int var : model.getVariables()) {
			if (retain.contains(var)) continue;
			deleted.add(var);
			model.removeVariable(var);
		}

		// deleted is already sorted (as model.getVariables is sorted)
	}

	@Override
	public GraphicalModel<F> execute(GraphicalModel<F> model, Int2IntMap evidence, int... query) {
		final GraphicalModel<F> copy = model.copy();
		executeInPlace(copy, evidence, query);
		return copy;
	}

	/**
	 * Update the evidence removing variable that where eliminated by the barren variables removal.
	 * This operation is done inplace
	 *
	 * @param evidence {@link Int2IntMap} the map of evidences to be updated
	 */
	public void filter(Int2IntMap evidence) {
		for (int var : deleted) evidence.remove(var);
	}

	public int[] filter(int[] variables) {
		IntList x = new IntArrayList(variables);
		x.removeAll(deleted);
		return x.toIntArray();
	}

	/**
	 * Get all the valid nodes.
	 *
	 * @param model    the model to be processed
	 * @param query    the variable that will be queried
	 * @param evidence may be null
	 * @return a {@link TIntSet} of visited variables
	 */
	private IntSet cutIndependent(final GraphicalModel<F> model, int[] query, IntSet evidence) {
		final IntSet locked = new IntOpenHashSet();
		final IntSet visited = new IntOpenHashSet();

		for (int node : query) locked.add(node);
		if (evidence != null) locked.addAll(evidence);

		DepthFirst independence_pass = new DepthFirst(model);
		final IntSet todelete = new IntOpenHashSet();

		independence_pass.setController(new SearchOperation() {

			/**
			 * Non locked nodes that have no children must be deleted and cannot
			 * be visited
			 */
			@Override
			public boolean visitChildren(int node) {
				if (todelete.contains(node)) return false;

				if (locked.contains(node) || hasChildren(node)) {
					return true;
				} else {
					todelete.add(node);
					return false;
				}
			}

			/**
			 * Same as the check before visiting the children
			 */
			@Override
			public boolean visitParents(int node) {
				return visitChildren(node);
			}

			@Override
			public void opening(int node, int from) {

			}

			@Override
			public void closing(int node, int from) {
				if (!todelete.contains(node)) visited.add(node);
			}

			private boolean hasChildren(int node) {
				for (int child : model.getChildren(node)) {
					if (!todelete.contains(child)) return true;
				}
				return false;
			}

		});

		independence_pass.visit(query);

		// find query disconnected stuff
		return visited;
	}
}
