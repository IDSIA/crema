package ch.idsia.crema.inference.jtree.algorithm.triangulation;

import ch.idsia.crema.utility.GraphUtil;
import ch.idsia.crema.utility.VertexPair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    13.02.2018 14:30
 */
public class MinDegreeOrdering extends Triangulate {

	/**
	 * Apply the Min-Degree-Ordering algorithm to a moralized {@link DirectedAcyclicGraph>} in order to find an
	 * elimination sequence and triangulate the input model.
	 *
	 * @return a triangulated {@link DirectedAcyclicGraph>}
	 */
	@Override
	public TriangulatedGraph exec() {
		if (network == null) throw new IllegalArgumentException("No model available");

		/*
		 * MinDegreeOrdering
		 *
		 * => keep track of the removed nodes
		 *
		 * 1) sort the nodes by cardinality (size of neighbourhood)
		 * 2) if cardinality == 1, remove node
		 * 3) if not triangular, add an edge
		 * 4) if triangular, remove node + neighbour edges
		 * 5) repeat from (1)
		 */

		// we are working with a "destructive" algorithm, so we make a copy of the current graph
		final SimpleGraph<Integer, DefaultEdge> copy = new SimpleGraph<>(DefaultEdge.class);
		GraphUtil.copy(network, copy);

		triangulated = new TriangulatedGraph();
		List<Integer> eliminationSequence = new ArrayList<>();

		// loop until we remove all the nodes from the graph
		while (!copy.vertexSet().isEmpty()) {
			// compute nodes cardinality
			Map<Integer, Integer> cardinality = new HashMap<>();
			copy.vertexSet().forEach(v -> cardinality.put(v, copy.edgesOf(v).size()));

			// sort by cardinality (ascending order)
			List<Integer> sortedByCardinality = cardinality.entrySet().stream()
					.sorted(Comparator.comparingInt(Map.Entry::getValue))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());

			Integer v = sortedByCardinality.get(0);
			Integer c = cardinality.get(v);

			// build neighbourhood
			Set<DefaultEdge> edges = copy.edgesOf(v);
			Set<Integer> neighbour = new HashSet<>();

			for (DefaultEdge edge : edges) {
				Integer source = copy.getEdgeSource(edge);
				Integer target = copy.getEdgeTarget(edge);

				Integer node = source.equals(v) ? target : source;

				neighbour.add(node);
			}

			// add current node to triangulated
			triangulated.addVertex(v);
			// add vertex to sequence
			eliminationSequence.add(v);

			if (c > 1) {
				// cardinality 2 or more: check if we have an edge that connect v to 2 nodes in the neighbourhood
				Iterator<Integer> it = neighbour.iterator();
				// no need to check for hasNext() since that with cardinality >= 2 we have at least 2 edges
				Integer i = it.next();
				Integer j = it.next();

				if (!copy.containsEdge(i, j)) {
					// add missing edge to working copy
					copy.addEdge(i, j);
				}
			}

			updateModels(copy, triangulated, v, edges);
		}

		triangulated.setEliminationSequence(eliminationSequence.stream().mapToInt(x -> x).toArray());

		return triangulated;
	}

	private void updateModels(SimpleGraph<Integer, DefaultEdge> in, TriangulatedGraph out, Integer v, Set<DefaultEdge> edges) {
		Set<VertexPair<Integer>> toRemove = new HashSet<>();

		for (DefaultEdge edge : edges) {
			// add all edges to triangulated node (should be only 1!)
			Integer source = in.getEdgeSource(edge);
			Integer target = in.getEdgeTarget(edge);
			out.addVertex(source);
			out.addVertex(target);
			out.addEdge(source, target);

			toRemove.add(new VertexPair<>(source, target));
		}

		// remove edge from working copy
		for (VertexPair<Integer> pair : toRemove) {
			in.removeEdge(pair.getFirst(), pair.getSecond());
		}

		// remove vertex from working copy
		in.removeVertex(v);
	}
}
