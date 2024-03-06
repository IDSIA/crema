package ch.idsia.crema.inference.ve.order;

import ch.idsia.crema.model.graphical.GraphicalModel;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

// TODO: this is the third version of TopologicalOrder* class. None is used, keep all of them?
public class TopologicalOrdering implements OrderingStrategy {

	@Override
	public int[] apply(GraphicalModel<?> model) {
		IntList visited = new IntArrayList();
		IntSet tovisit = new IntOpenHashSet();
		Int2ObjectMap<IntSet> parents = new Int2ObjectOpenHashMap<>();

		// find roots
		for (int var : model.getVariables()) {
			int[] parr = model.getParents(var);

			if (parr != null && parr.length > 0) {
				IntSet parent = new IntOpenHashSet(parr);
				parents.put(var, parent);
			} else {
				tovisit.add(var); // a root
			}
		}

		// run breath first
		while (!tovisit.isEmpty()) {
			// look for a node without unexplored parents
			for (int candidate : tovisit) {
				// first candidate with no parents wins
				if (!parents.containsKey(candidate) || parents.get(candidate).isEmpty()) {
					for (int child : model.getChildren(candidate)) {
						if (parents.containsKey(child)) {
							// remove the winning candidate from the children's parent list
							parents.get(child).remove(candidate);
						}

						// if (visited.contains(child)) impossible (a child is not visited yet)
						tovisit.add(child); // we can duplicate (to visit is a set)
					}

					visited.add(candidate);
					break;
				}
			}
		}
		return visited.toIntArray();
	}

}
