package ch.idsia.crema.inference.jtree.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:29
 */
public class EliminationTree {

	private TIntObjectMap<Node> nodes = new TIntObjectHashMap<>();
	private Set<Edge> edges = new HashSet<>();

	private TIntObjectMap<TIntArrayList> neighbour = new TIntObjectHashMap<>();

	public void add(int i, BayesianFactor factor) {
		Node node = new Node(i);
		node.setFactor(factor);
		nodes.put(i, node);
	}

	public void add(int i, int j) {
		Node nj = nodes.get(j);
		Node ni = nodes.get(i);

		ni.addNeighbour(nj);
		nj.addNeighbour(ni);

		if (!neighbour.containsKey(i)) neighbour.put(i, new TIntArrayList());
		if (!neighbour.containsKey(j)) neighbour.put(j, new TIntArrayList());

		neighbour.get(i).add(j);
		neighbour.get(j).add(i);

		edges.add(new Edge(ni, nj));
	}

	public Node get(int i) {
		return nodes.get(i);
	}

	private Node removeNode(int i) {
		neighbour.remove(i);
		return nodes.remove(i);
	}

	public Node remove(int r) {
		for (int key : neighbour.keys()) {
			if (key == r) continue;
			if (neighbour.get(key).size() == 1)
				return removeNode(key);
		}

		return null;
	}

	public int size() {
		return nodes.size();
	}

	public int[] vars() {
		TIntSet vars = new TIntHashSet();
		for (Node node : nodes.valueCollection()) {
			vars.addAll(node.vars());
		}

		return vars.toArray();
	}

	public int[] missingVariables(int[] variables) {
		TIntList vs = new TIntArrayList();
		int[] t = vars();

		for (int v : variables) {
			if (!ArraysUtil.contains(v, t))
				vs.add(v);
		}

		return vs.toArray();
	}

	public EliminationTree copy() {
		EliminationTree copy = new EliminationTree();
		copy.nodes.putAll(nodes);
		copy.edges.addAll(edges);
		copy.neighbour.putAll(neighbour);

		return copy;
	}
}
