package ch.idsia.crema.inference.fe;

import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.*;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:29
 */
public class EliminationTree {

	// TODO: maybe considering use the Graph interface also for trees?

	private static class DKey {
		int i, j;

		DKey(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DKey dKey = (DKey) o;
			return i == dKey.i &&
					j == dKey.j;
		}

		@Override
		public int hashCode() {

			return Objects.hash(i, j);
		}

		@Override
		public String toString() {
			return "(" + i + ", " + j + ")";
		}
	}

	// i -> nodes connected to i
	private final Map<Integer, Set<Integer>> neighbour = new HashMap<>();
	// i -> factor for i
	private final Map<Integer, BayesianFactor> factors = new HashMap<>();
	// [i, j] -> message from i to j
	private final Map<DKey, BayesianFactor> M = new HashMap<>();

	// in and out are referred to the current root node
	private Map<Integer, List<Integer>> edgesOut = new HashMap<>();
	private Map<Integer, List<Integer>> edgesIn = new HashMap<>();

	private final TIntSet vars = new TIntHashSet();

	private TIntIntMap evidence = new TIntIntHashMap();

	private int root;

	/**
	 * Add a new node to this tree or overwrite an existing one with a new factor.
	 *
	 * @param i      the index of the node
	 * @param factor the factor of this node
	 */
	public void addNode(int i, BayesianFactor factor) {
		factors.put(i, factor);
//		this.put(i, factor);
		vars.addAll(vars(i));
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
	 * Update the message mechanism by changing the root node. This changes the direction of the edges towards the given node.
	 *
	 * @param root the index of the new root node
	 */
	public void setRoot(int root) {
		if (!factors.containsKey(root))
			throw new IllegalArgumentException("Node " + root + " not found");

		this.root = root;

		// update edges direction for collect/distribute steps
		edgesOut = new HashMap<>();
		edgesIn = new HashMap<>();

		TIntStack stack = new TIntArrayStack();
		TIntSet visited = new TIntHashSet();

		stack.push(root);

		do {
			int n = stack.pop();
			visited.add(n);

			// update edges direction
			for (Integer i : neighbour.get(n)) {
				if (visited.contains(i))
					continue;

				edgesOut.computeIfAbsent(n, x -> new ArrayList<>()).add(i);
				edgesIn.computeIfAbsent(i, x -> new ArrayList<>()).add(n);

				stack.push(i);
			}
		} while (stack.size() > 0);
	}

	/**
	 * Return a new {@link Node} associated with the given index.
	 *
	 * @param i the index of the node
	 * @return a new object
	 */
	public Node getNode(int i) {
		Node n = new Node(i);
		n.setFactor(factors.get(i));
		n.getNeighbour().addAll(neighbour.get(i));

		return n;
	}

	/**
	 * @return an array with all the indices of the nodes
	 */
	public int[] getNodes() {
		TIntSet ids = new TIntHashSet();
		ids.addAll(neighbour.keySet());
		return ids.toArray();
	}

	/**
	 * Remove a node from this tree, eliminates its factor and remove from the neighbours the given index.
	 *
	 * @param i the index of the node to remove
	 * @return the removed node
	 */
	private Node removeNode(int i) {
		Node n = getNode(i);

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
	 * @param i node to visit
	 * @return the variables covered by node i
	 */
	public int[] vars(int i) {
		return factors.get(i).getDomain().getVariables();
	}

	/**
	 * A separator defines a set of variables for each edge in an elimination tree.
	 *
	 * @param i first node
	 * @param j second node
	 * @return the separator between two nodes
	 */
	public int[] separator(int i, int j) {
		int[] SIJ = exploreSeparator(i, j);
		int[] SJI = exploreSeparator(j, i);

		// TODO: cache the results?

		return ArraysUtil.intersectionSorted(SIJ, SJI);
	}

	private int[] exploreSeparator(int i, int j) {
		TIntSet separator = new TIntHashSet();

		TIntStack nodeToVisit = new TIntArrayStack();
		TIntSet nodeVisited = new TIntHashSet();

		nodeToVisit.push(i);

		while (nodeToVisit.size() > 0) {
			int n = nodeToVisit.pop();

			separator.addAll(vars(n));
			nodeVisited.add(n);

			for (Integer x : neighbour.get(n)) {
				if (!nodeVisited.contains(x) && x != j)
					nodeToVisit.push(x);
			}
		}

		return ArraysUtil.sort(separator.toArray());
	}

	/**
	 * A cluster defines a set of variables for each node in the tree.
	 *
	 * @param i node in the tree
	 * @return the cluster associated with the node
	 */
	public int[] cluster(int i) {

		TIntSet cluster = new TIntHashSet();

		cluster.addAll(vars(i));
		for (Integer j : neighbour.get(i)) {
			cluster.addAll(separator(i, j));
		}

		return ArraysUtil.sort(cluster.toArray());
	}

	private BayesianFactor phi(int i) {
		BayesianFactor factor = factors.get(i);

		if (evidence.containsKey(i)) {
			Strides domain = factor.getDomain();

			int size = domain.getCardinality(i);
			double[] data = new double[size]; // initialized to 0
			data[evidence.get(i)] = 1.;

			BayesianFactor e = new BayesianFactor(DomainBuilder.var(i).size(size).strides(), data);

			factor = factor.combine(e);
		}

		return factor;
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

	public BayesianFactor collect() {
		// collect phi(root)
		BayesianFactor phi = phi(root);

		System.out.println("collecting phi(" + root + ")");

		// for each edge that ends in root
		for (Integer i : edgesOut.get(root)) {
			// collect the message from neighbour i to root
			BayesianFactor M = collect(i, root);

			System.out.println("combine phi(" + root + ") with + M(" + i + ", " + root + ")");

			// combine phi(root) with M(i, root)
			phi = phi.combine(M);
		}

//		phis.put(root, phi);

		System.out.println("returning phi(" + root + "): " + Arrays.toString(phi.getData()));

		return phi;
	}

	/**
	 * The edge for collect is in the form (i -> j)
	 *
	 * @param i the current node
	 * @param j the previous node
	 * @return the message from i to j
	 */
	private BayesianFactor collect(int i, int j) {
		// collect phi(i)
		BayesianFactor phi = phi(i);

		System.out.println("collecting phi(" + i + ")");

		// if we have outbound edges from this node we need to compute the messages
		if (edgesOut.containsKey(i)) {
			// we need the message from the outs nodes
			List<Integer> outs = edgesOut.get(i);

			System.out.println("edges: " + outs);

			// for each edge that ends in this node
			for (Integer o : outs) {
				// collect the message from o to j
				BayesianFactor M = collect(o, i);

				System.out.println("combine phi(" + i + ") with + M(" + o + ", " + i + ")");

				// combine the message with the current phi
				phi = phi.combine(M);
			}
		}

//		phis.put(i, phi);

		// compute the message by projecting phi over the separator(i, j)
		BayesianFactor Mij = project(phi, separator(i, j));
		M.put(new DKey(i, j), Mij);

		System.out.println("returning M(" + i + ", " + j + "): " + Arrays.toString(Mij.getData()));

		return Mij;
	}

	public void distribute() {

		// j is the destination node for the edge (root -> j)
		for (Integer j : neighbour.get(root)) {

			// compute the message by projecting phi(root) over the separator (root, j)
			BayesianFactor phi = phi(root);

			for (Integer i : neighbour.get(root)) {
				if (!i.equals(j))
					phi = phi.combine(M.get(new DKey(i, root)));
			}

			BayesianFactor Mij = project(phi, separator(root, j));
			// distribute the message M(root,j) to node j
			distribute(root, j, Mij);
		}
	}

	/**
	 * @param k   source node
	 * @param i   current node
	 * @param Mki the message from k to i
	 */
	private void distribute(int k, int i, BayesianFactor Mki) {

		System.out.println("distributing M(" + k + ", " + i + "): " + Arrays.toString(Mki.getData()));
		M.put(new DKey(k, i), Mki);

		// if we have nodes that need the message from this node i
		for (Integer j : neighbour.get(i)) {
			if (j != k) {
				BayesianFactor phi = phi(i);

				for (Integer m : neighbour.get(i)) {
					if (!m.equals(j))
						phi = phi.combine(M.get(new DKey(m, i)));
				}

				BayesianFactor Mij = project(phi, separator(i, j));

				distribute(i, j, Mij);
			}
		}
	}

	private BayesianFactor project(BayesianFactor phi, int... Q) {

		TIntSet variables = new TIntHashSet(phi.getDomain().getVariables());
		for (int q : Q) {
			variables.remove(q);
		}

		for (int v : variables.toArray())
			phi = phi.marginalize(v);

		return phi;
	}

	public BayesianFactor compute(int i) {
		BayesianFactor phi = phi(i);

		System.out.println("compute phi(" + i + "): " + Arrays.toString(phi.getData()));

		if (edgesIn.containsKey(i)) {
			for (Integer k : edgesIn.get(i)) {
				BayesianFactor m = M.get(new DKey(k, i));

				System.out.println("M(" + k + ", " + i + "): " + m);

				phi = phi.combine(m);
			}
		}

		return phi;
	}

	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}
}
