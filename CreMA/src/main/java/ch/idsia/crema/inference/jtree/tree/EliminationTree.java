package ch.idsia.crema.inference.jtree.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:29
 */
public class EliminationTree {

	private Map<Integer, Set<Integer>> neighbour = new HashMap<>();
	private Map<Integer, BayesianFactor> factors = new HashMap<>();

	/**
	 * Add a new node to this tree or overwrite an existing one with a new factor.
	 *
	 * @param i      the index of the node
	 * @param factor the factor of this node
	 */
	public void addNode(int i, BayesianFactor factor) {
		factors.put(i, factor);
	}

	/**
	 * Add a new edge between two nodes in this tree. If the nodes does not exist, an {@link IllegalArgumentException}
	 * will be raised.
	 *
	 * @param i the index of the first node
	 * @param j the index of the second node
	 */
	public void addEdge(int i, int j) {
		if (!factors.containsKey(i)) throw new IllegalArgumentException("Node " + i + " does not exist in this tree");
		if (!factors.containsKey(j)) throw new IllegalArgumentException("Node " + j + " does not exist in this tree");

		neighbour.computeIfAbsent(i, x -> new HashSet<>()).add(j);
		neighbour.computeIfAbsent(j, x -> new HashSet<>()).add(i);
	}

	/**
	 * Return a new {@link Node} associated with the given index.
	 *
	 * @param i the index of the node
	 * @return a new object
	 */
	public Node get(int i) {
		Node n = new Node(i);
		n.setFactor(factors.get(i));
		n.getNeighbour().addAll(neighbour.get(i));

		return n;
	}

	/**
	 * Remove a node from this tree, eliminates its factor and remove from the neighbours the given index.
	 *
	 * @param i the index of the node to remove
	 * @return the removed node
	 */
	private Node removeNode(int i) {
		Node n = get(i);

		factors.remove(i);
		neighbour.remove(i);
		for (Set<Integer> set : neighbour.values()) {
			set.remove(i);
		}
		return n;
	}

	/**
	 * Search and remove the first occurrences of a {@link Node} that have a single neighbour and its index is not the
	 * given root index.
	 *
	 * @param r node root to avoid
	 * @return the first occurrence of a node that has only one neighbour, null if nothing found
	 */
	public Node remove(int r) {
		for (int key : neighbour.keySet()) {
			if (key == r) continue;
			if (neighbour.get(key).size() == 1)
				return removeNode(key);
		}

		return null;
	}

	/**
	 * @return current number of node in this tree
	 */
	public int size() {
		return factors.size();
	}

	/**
	 * @return the currents variables covered by this tree
	 */
	public int[] vars() {
		TIntSet vars = new TIntHashSet();
		for (BayesianFactor factor : factors.values()) {
			vars.addAll(factor.getDomain().getVariables());
		}

		return vars.toArray();
	}

	/**
	 * Given a set of variables, check if these variables are covered by this tree. Returns an array with the input
	 * variables that are not covered by this tree.
	 *
	 * @param variables variables to search
	 * @return an array with the variables that are NOT covered by this tree.
	 */
	public int[] missingVariables(int[] variables) {
		TIntList vs = new TIntArrayList();
		int[] t = vars();

		for (int v : variables) {
			if (!ArraysUtil.contains(v, t))
				vs.add(v);
		}

		return vs.toArray();
	}

	/**
	 * @return a copy of this tree
	 */
	public EliminationTree copy() {
		EliminationTree copy = new EliminationTree();
		copy.neighbour.putAll(neighbour);
		copy.factors.putAll(factors);

		return copy;
	}
}
