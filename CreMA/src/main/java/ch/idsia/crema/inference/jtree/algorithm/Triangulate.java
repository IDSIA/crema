package ch.idsia.crema.inference.jtree.algorithm;

import ch.idsia.crema.model.graphical.SparseUndirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 11:34
 */
public class Triangulate {

	private Graph<Integer, DefaultEdge> model;

	public void setModel(Graph<Integer, DefaultEdge> model) {
		this.model = model;
	}

	public SparseUndirectedGraph run() {

		/*
		 * Min-Fill (or something similar...)
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
		final SparseUndirectedGraph copy = new SparseUndirectedGraph();
		model.vertexSet().forEach(copy::addVertex);
		model.edgeSet().forEach(edge -> copy.addEdge(model.getEdgeSource(edge), model.getEdgeTarget(edge)));

		final SparseUndirectedGraph triangulated = new SparseUndirectedGraph();

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
		Set<DefaultEdge> edges = model.edgesOf(v);

		Set<Integer> neighbour = new HashSet<>();

		for (DefaultEdge edge : edges) {
			Integer source = model.getEdgeSource(edge);
			Integer target = model.getEdgeTarget(edge);

			Integer node = source.equals(v) ? target : source;

			neighbour.add(node);
		}

		// check if we have an edge that connect v to 2 nodes in the neighbourhood
		Iterator<Integer> it = neighbour.iterator();
		Integer i = it.next();

		while (it.hasNext()) {
			Integer j = it.next();

			if (model.containsEdge(i, j)) {
				// v i j is a triangle!
				addTriangle(triangulated, v, i, j);
			}

			i = j;
		}

		// TODO: loop and finish!

		return triangulated;
	}

	private void addNode(SparseUndirectedGraph graph, Integer i, Integer j) {
		System.out.println("Add node: " + i + " " + j);
		graph.addVertex(i);
		graph.addVertex(j);
		graph.addEdge(i, j);
	}

	private void addTriangle(SparseUndirectedGraph graph, Integer v, Integer i, Integer j) {
		System.out.println("Add triangle: " + v + " " + i + " " + j);
		graph.addVertex(v);
		graph.addVertex(i);
		graph.addVertex(j);
		graph.addEdge(v, i);
		graph.addEdge(v, j);
		graph.addEdge(i, j);
	}
}
