package ch.idsia.crema.inference.ve.order;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class TopologicalOrdering  implements OrderingStrategy {
	
	@Override
	public int[] apply(GraphicalModel<?> model) {
		TIntList visited = new TIntArrayList();
		TIntSet tovisit = new TIntHashSet();
		TIntObjectMap<TIntHashSet> parents = new TIntObjectHashMap<>();
		
        // find roots
		for (int var : model.getVariables()) {
			int[] parr = model.getParents(var);
		
			if (parr != null && parr.length > 0) {
				TIntHashSet parent = new TIntHashSet(parr);
				parents.put(var, parent);
			} else {
				tovisit.add(var); // a root
			}
		}
		
        // run breath first
        while(!tovisit.isEmpty()) {
        	// look for a node without unexplored parents
        	for (int candidate : tovisit.toArray()) {
        		// first candidate with no parents wins
        		if (!parents.containsKey(candidate) || parents.get(candidate).isEmpty()) {
        			for (int child : model.getChildren(candidate)) {
        				if (parents.containsKey(child)) {
        					// remove the winning candidate from the children's parent list
        					parents.get(child).remove(candidate);
        				}
        				
        				// if (visited.contains(child)) impossible (a child is not visited yet)
        				tovisit.add(child); // we can duplicate (tovisit is a set)
        			}

        			visited.add(candidate);
    				break;
        		}
        	}
        }
        return visited.toArray();
    }

}
