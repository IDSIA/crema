package ch.idsia.crema.inference.ve.order;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class TopologicalOrder2  implements OrderingStrategy {
	

	@Override
	public int[] apply(GraphicalModel<?> model) {
        TIntObjectMap<TIntArrayList> E = new TIntObjectHashMap<>(model.getVariablesCount()); // Container for the edges
        for (int i : model.getVariables()) {
            E.put(i, new TIntArrayList());
        }
        
        TIntArrayList P = new TIntArrayList(model.getVariablesCount()); // Parent set dom_size residual for each node
        TIntArrayList L = new TIntArrayList(); // Empty list that will contain the sorted elements
        TIntArrayList S = new TIntArrayList(); // Set of all nodes with no incoming edges
        for (int i : model.getVariables()) {
            int[] parents = model.getParents(i);
            // Add root notes
            if (parents.length == 0) {
             S.add(i);
            }
            P.add(parents.length);
            for (int parent : parents) {
                E.get(parent).add(i);
            }
        }
        while (!S.isEmpty()) { // while S is non-empty do
            int n = S.iterator().next(); // remove a node n from S
            S.remove(n);
            L.add(n);
            for (int i = 0; i < E.get(n).size(); i++) { // for each child of node
                int c = E.get(n).get(i);
                int s = P.get(c) - 1; // reduce size of parent set
                if (s == 0) {
                    S.add(c);
                } // if m has no incoming edges then
                P.set(c, s);
            }
        }
        return L.toArray();
    }

}
