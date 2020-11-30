package ch.idsia.crema.inference.ve.order;


import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.VertexPair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Clique creation and ordering strategy generator based in the Minimum Fill In Algorithm.
 * TODO this could make use of a local-search of the search framework.
 * 
 * @author david 
 *
 */
public class MinFillOrdering implements OrderingStrategy {
	private int[] sequence;
	
	@Override
	public int[] apply(GraphicalModel<?> model) {
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		loadMoralized(model, graph);
		triangulate(graph);
		
		return sequence;
	}

	private void loadMoralized(GraphicalModel<?> model, SimpleGraph<Integer, DefaultEdge> graph) {
		for (int var : model.getVariables()) {
			graph.addVertex(var);
		}

		for (int var : model.getVariables()) {
			
			for (int parent : model.getParents(var)) {
				graph.addEdge(var, parent);
				
				// marry the parents
				for (int other : model.getParents(var)) {
					if (other == parent) break;
					graph.addEdge(parent, other);
				}
			}
		}
	}
	

	/**
	 * Triangulate the graph using the minfill algorithm.
	 * 
	 * @param graph
	 * @return
	 */
	private Collection<Set<Integer>> triangulate(SimpleGraph<Integer, DefaultEdge> graph) {
		// make a copy of the vertices set
		ArrayList<Integer> todo = new ArrayList<>(graph.vertexSet());

		SimpleGraph<Integer, DefaultEdge> triag = graph;
		ArrayList<Set<Integer>> cliques = new ArrayList<>();

		int item = 0;
		sequence = new int[todo.size()];

		while (!todo.isEmpty()) {

			int removedItems = 1;
			
			while (removedItems != 0 && !todo.isEmpty()) {

				// remove all simplical nodes
				removedItems = 0;
				List<VertexPair<Integer>> best = null;
				
				ListIterator<Integer> todoIterator = todo.listIterator();
				while(todoIterator.hasNext()) {
					Integer node = todoIterator.next();
					
					// if they are all connected the node can be removed
					List<VertexPair<Integer>> test = getMissingLinks(triag, node);

					// node is fully connected
					if (test.isEmpty()) {

						Set<Integer> candidate = new HashSet<>();
						candidate.add(node);

						for (DefaultEdge edge : triag.edgesOf(node)) {
							Integer source = triag.getEdgeSource(edge);
							Integer target = triag.getEdgeTarget(edge);
					
							Integer neighbour = ((node == source) ? target : source);
							candidate.add(neighbour);
						}

						// if the candidate is not included in an existing
						// clique we need to add it
						boolean found = false;
						for (Set<Integer> anotherClique : cliques) {
							if(anotherClique.containsAll(candidate)) {
								found = true;
								break;
							}
						}
						
						if (!found) {
							cliques.add(candidate);
						}
						
						sequence[item++]=node;
						
						todoIterator.remove(); // remove last returned element (node)
						triag.removeVertex(node);
						++removedItems;
					} else if (best == null || best.size() >= test.size()) {
						// we are looking for the test triangulization that would add the least fill-in arcs
						best = test;
					}
				}
				
				if (removedItems == 0 && best != null) {
					for (VertexPair<Integer> vertex : best) {
						triag.addEdge(vertex.getFirst(), vertex.getSecond());
					}
					// FIXME: best node should be simplical and should be removable
				}
			}
		}
		
		
		return cliques;
	}

	/**
	 * Return a list of ILinks that will be needed to make the node simplical
	 */
	private List<VertexPair<Integer>> getMissingLinks(SimpleGraph<Integer, DefaultEdge> graph, Integer node) {
		ArrayList<VertexPair<Integer>> missing = new ArrayList<>();

		ArrayList<DefaultEdge> neighbours = new ArrayList<>(graph.edgesOf(node));
		for (int firstIndex = 0; firstIndex < neighbours.size() - 1; ++firstIndex) {
			DefaultEdge firstEdge = neighbours.get(firstIndex);
			Integer source = graph.getEdgeSource(firstEdge);
			Integer target = graph.getEdgeTarget(firstEdge);

			Integer first = (node == source) ? target : source;

			// continue if the node has been removed
//			if (removed.contains(first))
//				continue;

			for (int secondIndex = firstIndex + 1; secondIndex < neighbours.size(); ++secondIndex) {
				DefaultEdge secondEdge = neighbours.get(secondIndex);
				Integer source2 = graph.getEdgeSource(secondEdge);
				Integer target2 = graph.getEdgeTarget(secondEdge);

				Integer second = (node == source2) ? target2 : source2;

//				if (removed.contains(second))
//					continue;

				if (!graph.containsEdge(first, second)) {
					missing.add(new VertexPair<>(first, second));
				}
			}
		}
		return missing;
	}
}
