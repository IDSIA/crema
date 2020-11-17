package ch.idsia.crema.inference.jtree.algorithm.updating;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.algorithm.cliques.Clique;
import ch.idsia.crema.inference.jtree.algorithm.junction.JunctionTree;
import ch.idsia.crema.inference.jtree.algorithm.junction.Separator;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:03
 */
public class BeliefPropagation {

	private final SparseDirectedAcyclicGraph network;
	private final TIntObjectMap<BayesianFactor> factors;

	JunctionTree junctionTree;

	private boolean fullyPropagated = false;

	private TIntIntMap evidence = new TIntIntHashMap();

	public BeliefPropagation(BayesianNetwork bn) {
		this.network = bn.getNetwork();
		this.factors = bn.getFactorsMap();
		init();
	}

	/**
	 * Builds the {@link JunctionTree} required for the algorithm.
	 */
	public void init() {
		GraphToJunctionTreePipe pipeline = new GraphToJunctionTreePipe();
		pipeline.setInput(network);
		junctionTree = pipeline.exec();
		fullyPropagated = false;
	}

	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
		fullyPropagated = false;
	}

	public void clearEvidence() {
		evidence = new TIntIntHashMap();
		fullyPropagated = false;
	}

	private Clique getRoot(int variable) {
		Optional<Clique> minRoot = junctionTree.vertexSet()
				.stream()
				.filter(c -> c.contains(variable))
				.min(Comparator.comparingInt(c -> c.getVariables().length));

		if (minRoot.isEmpty())
			throw new IllegalArgumentException("Variable " + variable + " not found in model");

		return minRoot.get();
	}

	/**
	 * @param variable variable to query
	 * @return the marginal probability of the given query
	 */
	public BayesianFactor query(int variable) {
		BayesianFactor f;

		if (!fullyPropagated)
			f = collectingEvidence(variable);
		else
			f = phi(getRoot(variable));

		// marginalize out what is not needed
		int[] ints = IntStream.of(f.getDomain().getVariables()).filter(x -> x != variable).toArray();

		return f.marginalize(ints).normalize();
	}

	/**
	 * Performs a full update of the network, considering the given variable as root and query node.
	 *
	 * @return the marginalized probability of the query node.
	 */
	public BayesianFactor fullPropagation() {
		checks();

		Integer variable = network.vertexSet().iterator().next();

		BayesianFactor f = collectingEvidence(variable);
		distributingEvidence(variable);

		return f;
	}

	/**
	 * Executes teh collection step of the Belief Propagation algorithm.
	 *
	 * @param variable the variable to consider the root node
	 * @return the new root variable of the network
	 */
	public BayesianFactor collectingEvidence(int variable) {
		checks();

		Clique root = getRoot(variable);

		// collect messages
		Stream<BayesianFactor> psis = junctionTree.edgesOf(root).stream()
				.map(edge -> {
					Clique source = junctionTree.getEdgeSource(edge);
					Clique target = junctionTree.getEdgeTarget(edge);

					Clique i = root == source ? target : source;

					return collect(/* from */ i, /* to */root);
				});

		Stream<BayesianFactor> phis = Stream.of(phi(root));

		// combine by potential
		BayesianFactor f = Stream.concat(phis, psis)
				.reduce(BayesianFactor::combine)
				.orElseThrow(() -> new IllegalStateException("No factor after combination with messages"));

		// marginalize out what is not needed
		int[] ints = IntStream.of(f.getDomain().getVariables()).filter(x -> x != variable).toArray();

		return f.marginalize(ints).normalize();
	}

	/**
	 * Executes the distribution step of the Belief Propagation algorithm.
	 *
	 * @param variable the variable to consider the root node
	 */
	public void distributingEvidence(int variable) {
		checks();

		Clique root = getRoot(variable);
		BayesianFactor phi = phi(root);

		junctionTree.outgoingEdgesOf(root).forEach(edge -> {
			Clique source = junctionTree.getEdgeSource(edge);
			Clique target = junctionTree.getEdgeTarget(edge);

			Clique i = root == source ? target : source;

			Separator S = junctionTree.getEdge(i, root);
			BayesianFactor Mij = project(phi, S);

			// distribute the message M(root,j) to node j
			distribute(root, i, Mij);
		});

		fullyPropagated = true;
	}

	private void checks() {
		if (network == null) throw new IllegalArgumentException("No network available");
		if (factors == null) throw new IllegalArgumentException("No factors available");
		if (junctionTree == null) throw new IllegalStateException("No JunctionTree available");
	}

	/**
	 * @param j from this node
	 * @param k to this node
	 * @return the message Mjk
	 */
	private BayesianFactor collect(Clique j, Clique k) {
		// collect phi(j)
		BayesianFactor phi = phi(j);

		for (Separator edge : junctionTree.edgesOf(j)) {
			Clique source = junctionTree.getEdgeSource(edge);
			Clique target = junctionTree.getEdgeTarget(edge);

			// if we have inbound edges for this node j, we compute the message
			Clique i = j == source ? target : source;

			// ignore messages inbound from k
			if (i.equals(k)) continue;

			BayesianFactor Mij = collect(i, j);

			phi.combine(Mij);
		}

		// compute the message by projecting phi over the separator(i, j)
		Separator S = junctionTree.getEdge(j, k);
		BayesianFactor Mjk = project(phi, S);
		S.setMessage(j, Mjk);

		return Mjk;
	}

	/**
	 * @param k   from this node
	 * @param i   to this node
	 * @param Mki the message Mki
	 */
	private void distribute(Clique k, Clique i, BayesianFactor Mki) {
		junctionTree.getEdge(k, i).setMessage(k, Mki);

		BayesianFactor phi = phi(i).combine(Mki);

		// if we have nodes that need the message from this node i
		for (Separator edge : junctionTree.edgesOf(i)) {
			Clique source = junctionTree.getEdgeSource(edge);
			Clique target = junctionTree.getEdgeTarget(edge);

			Clique j = i == source ? target : source;

			// ignore message outgoing to k
			if (j.equals(k)) continue;

			BayesianFactor Mij = project(phi, junctionTree.getEdge(i, j));

			distribute(i, j, Mij);
		}
	}

	/**
	 * Compute the potential phi of a given {@link Clique}. Evidence is considered there.
	 *
	 * @param clique input clique
	 * @return a {@link BayesianFactor} which is a combination of all the factors and the evidences included in the Clique
	 */
	private BayesianFactor phi(Clique clique) {
		BayesianFactor factor = IntStream.of(clique.getVariables())
				.mapToObj(factors::get)
				.reduce(BayesianFactor::combine)
				.orElseThrow(() -> new IllegalStateException("Empty BayesianFactor after reduce"));

		return factor.filter(evidence);
	}

	/**
	 * Project the variables of the given {@link Separator} on the given potential.
	 *
	 * @param phi input potential
	 * @param S   separator to consider
	 * @return a marginalized and normalized {@link BayesianFactor}
	 */
	private BayesianFactor project(BayesianFactor phi, Separator S) {
		int[] ints = Arrays.stream(phi.getDomain().getVariables())
				.filter(x -> ArraysUtil.contains(x, S.getVariables()))
				.toArray();

		return phi.marginalize(ints).normalize();
	}
}
