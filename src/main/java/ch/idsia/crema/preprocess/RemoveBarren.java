package ch.idsia.crema.preprocess;

import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.search.SearchOperation;
import ch.idsia.crema.search.impl.DepthFirst;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class RemoveBarren {
	
	private int[] deleted;

	public RemoveBarren() {
		
	}
	@SuppressWarnings("rawtypes")
	public <M extends GraphicalModel> M  execute(M model, int query, int... evidence) {
		return execute(model, new int[] { query }, evidence);
	}
	
	@SuppressWarnings("rawtypes")
	public <M extends GraphicalModel> M  execute(M model, int query, TIntIntMap evidence) {
		return execute(model, new int[] { query }, evidence.keys());
	}
	
	@SuppressWarnings("rawtypes")
	public <M extends GraphicalModel> M  execute(M model, int[] query, TIntIntMap evidence) {
		return execute(model, query, evidence.keys());
	}
	
	@SuppressWarnings("rawtypes")
	public <M extends GraphicalModel> M execute(M model, int[] query, int... evidence) {
		@SuppressWarnings("unchecked")
		M copy = (M) model.copy();
		executeInline(copy, query, evidence);
		return copy;
	}
	
	/**
	 * Remove barren variable from the specified model.
	 * 
	 * @param model
	 * @param query
	 * @param evidence
	 */
	public void executeInline(GraphicalModel<?> model, int query, TIntIntMap evidence) {
		executeInline(model, new int[] { query }, evidence.keys());
	}
	public void executeInline(GraphicalModel<?> model, int[] query, TIntIntMap evidence) {
		executeInline(model, query, evidence.keys());
	}
	
	public void executeInline(GraphicalModel<?> model, int[] query, int... evidence) {
		TIntSet retain = cutIndependent(model, query, evidence);
		TIntArrayList todelete = new TIntArrayList();
		
		for (int var : model.getVariables()) {
			if (retain.contains(var)) continue;
			todelete.add(var);
			model.removeVariable(var);
		}
		
		deleted = todelete.toArray();
		// deleted is already sorted (as model.getVariables is sorted)
	}

	/** 
	 * Update the evidence removing variable that where eliminated by the barren variables removal.
	 * This operation is done inplace
	 * 
	 * @param evidence {@link TIntIntMap} the map of evidences to be updated
	 */
	public void filter(TIntIntMap evidence) {
		for (int var : deleted) evidence.remove(var);
	}

	public int[] filter(int[] variables) {
		TIntArrayList x = new TIntArrayList(variables);
		x.removeAll(deleted);
		return x.toArray();
	}


	/** 
	 * Get all the valid nodes
	 * 
	 * @param model
	 * @param query
	 * @param evidence may be null
	 * @return
	 */
	private TIntSet cutIndependent(final GraphicalModel<?> model, int[] query, int[] evidence) {
		
		final TIntSet locked = new TIntHashSet();
		final TIntSet visited = new TIntHashSet();
		
		for (int node : query) locked.add(node);
		if (evidence != null) locked.addAll(evidence);
		
		DepthFirst independence_pass = new DepthFirst(model);
		final TIntSet todelete = new TIntHashSet();
		
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
	
	/**
	 * Returns a copy of the deleted nodes array.
	 * @return
	 */
	public int[] getDeleted() {
		return deleted;
	}
}
