package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.junction.JunctionTree;
import ch.idsia.crema.inference.bp.junction.Separator;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:03
 */
public class BeliefPropagation<F extends Factor<F>> {

	private final DAGModel<F> model;

	private JunctionTree<F> junctionTree;

	private boolean fullyPropagated = false;

	private TIntIntMap evidence = new TIntIntHashMap();

	private Map<Clique, Set<F>> potentialsPerClique = new HashMap<>();

	public BeliefPropagation(DAGModel<F> model) {
		this.model = model;
		init();
	}

	/**
	 * Builds the {@link JunctionTree} required for the algorithm.
	 */
	public void init() {
		GraphToJunctionTreePipe<F> pipeline = new GraphToJunctionTreePipe<>();
		pipeline.setInput(model.getNetwork());
		junctionTree = pipeline.exec();

		// distribute potentials
		TIntObjectMap<Clique> factorsPerClique = new TIntObjectHashMap<>();

		if (junctionTree.vertexSet().size() == 2) {
			junctionTree.vertexSet().forEach(clique -> {
				for (int v : clique.getVariables()) {

					factorsPerClique.put(v, clique);
				}
			});
		} else {
			final List<Clique> cliques = junctionTree.vertexSet().stream()
					.sorted(Comparator.comparingInt(a -> junctionTree.edgesOf(a).size()))
					.collect(Collectors.toList());

			cliques.forEach(clique -> {
				final Set<Separator<F>> edges = junctionTree.edgesOf(clique);
				final int[] ints = edges.stream()
						.map(Separator::getVariables)
						.flatMapToInt(Arrays::stream)
						.toArray();
				for (int v : clique.getVariables()) {
					if (edges.size() == 1 && ArraysUtil.contains(v, ints)) {
						continue;
					}
					factorsPerClique.put(v, clique);
				}
			});
		}

		model.getFactorsMap().forEachEntry(
				(v, f) -> potentialsPerClique.computeIfAbsent(factorsPerClique.get(v), i -> new HashSet<>()).add(f)
		);

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
		return junctionTree.vertexSet()
				.stream()
				.filter(c -> c.contains(variable))
				.min(Comparator.comparingInt(c -> c.getVariables().length))
				.orElseThrow(() -> new IllegalArgumentException("Variable " + variable + " not found in model"));
	}

	/**
	 * @param variable variable to query
	 * @return the marginal probability of the given query
	 */
	public F query(int variable) {
		F f;

//		if (fullyPropagated) {
//			f = phi(getRoot(variable));
//		} else {
		f = collectingEvidence(variable);
//		}

		// marginalize out what is not needed
//		int[] ints = IntStream.of(f.getDomain().getVariables()).filter(x -> x != variable).toArray();

		return f;
	}

	public F query(int variable, TIntIntMap evidence) {
		setEvidence(evidence);
		return query(variable);
	}

	/**
	 * Performs a full update of the network, considering the given variable as root and query node.
	 *
	 * @return the marginalized probability of the query node.
	 */
	public F fullPropagation() {
		Integer variable = model.getNetwork().vertexSet().iterator().next();

		return fullPropagation(variable);
	}

	public F fullPropagation(int variable) {
		checks();

		F f = collectingEvidence(variable);
		distributingEvidence(variable);

		return f;
	}

	private Clique cliqueFromDirection(Clique from, Separator<F> edge) {
		Clique source = junctionTree.getEdgeSource(edge);
		Clique target = junctionTree.getEdgeTarget(edge);

		return from == source ? target : source;
	}

	/**
	 * Executes teh collection step of the Belief Propagation algorithm.
	 *
	 * @param variable the variable to consider the root node
	 * @return the new root variable of the network
	 */
	public F collectingEvidence(int variable) {
		checks();

		final Clique root = getRoot(variable);

		// collect messages
		F psis = junctionTree.edgesOf(root).stream()
				.map(edge -> cliqueFromDirection(root, edge))
				.map(i -> collect(/* from */ i, /* to */root))
				.reduce(F::combine)
				.orElseThrow(() -> new IllegalStateException("No factor after combination with messages"));

		F phis = phi(root);

		// combine by potential
		F f = psis.combine(phis);

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
		F phi = phi(root);

		junctionTree.outgoingEdgesOf(root).forEach(edge -> {
			Clique i = cliqueFromDirection(root, edge);

			Separator<F> S = junctionTree.getEdge(i, root);
			F Mij = project(phi, S);

			// distribute the message M(root, i) to node i
			distribute(root, i, Mij);
		});

		fullyPropagated = true;
	}

	private void checks() {
		if (model == null) throw new IllegalStateException("No network available");
		if (junctionTree == null) throw new IllegalStateException("No JunctionTree available");
	}

	/**
	 * This method is called from a k node that request a message from a j node. This is what happens in the j node. We
	 * want to send a message from the j node to the k node. We first collect recursively all the messages coming from
	 * other non-k nodes to this node. Then we combine all the messages Ψ with the potentials Φ of the j node. Finally,
	 * we set and return the message for the k node.
	 *
	 * @param j from this node
	 * @param k to this node
	 * @return the message Mjk
	 */
	private F collect(Clique j, Clique k) {
		// collect and combine messages psi to j
		Optional<F> psis = junctionTree.edgesOf(j).stream()
				.map(edge -> cliqueFromDirection(j, edge))
				.filter(i -> !i.equals(k))
				.map(i -> collect(/* from */ i, /* to */ j))
				.reduce(F::combine);

		// collect and combine potentials phi of j
		F phis = phi(j);

		// the new message is the marginalization over the separator
		Separator<F> S = junctionTree.getEdge(j, k);
		int[] ints = cliqueNotInSeparator(j, S);
		F Mjk;

		if (psis.isPresent()) {
			Mjk = phis.combine(psis.get()).marginalize(ints);
		} else {
			Mjk = phis.marginalize(ints);
		}

		S.setMessage(/* from */ j, Mjk);

		System.out.println("PSI(" + j + " -> " + k + ":" + S + ") = " + Mjk);

		return Mjk;
	}

	/**
	 * @param k   from this node
	 * @param i   to this node
	 * @param Mki the message Mki
	 */
	private void distribute(Clique k, Clique i, F Mki) {
		junctionTree.getEdge(k, i).setMessage(k, Mki);

		F phi = phi(i).combine(Mki);

		// if we have nodes that need the message from this node i
		for (Separator<F> edge : junctionTree.edgesOf(i)) {
			Clique j = cliqueFromDirection(i, edge);

			// ignore message outgoing to k
			if (j.equals(k)) continue;

			F Mij = project(phi, junctionTree.getEdge(i, j));

			distribute(i, j, Mij);
		}
	}

	public int[] cliqueNotInSeparator(Clique c, Separator<F> S) {
		return Arrays.stream(c.getVariables())
				.filter(x -> !ArraysUtil.contains(x, S.getVariables()))
				.toArray();
	}

	/**
	 * Compute the potential phi of a given {@link Clique}. Evidence is considered there.
	 *
	 * @param clique input clique
	 * @return a {@link F} which is a combination of all the factors and the evidences included in the Clique
	 */
	private F phi(Clique clique) {
		final F phi = potentialsPerClique.get(clique).stream()
				.map(f -> f.filter(evidence))
				.reduce(F::combine)
				.orElseThrow(() -> new IllegalStateException("Empty F after combination"));

		System.out.println("PHI(" + clique + ") = " + phi);

		return phi;
	}

	/**
	 * Project the variables of the given {@link Separator} on the given potential.
	 *
	 * @param phi input potential
	 * @param S   separator to consider
	 * @return a marginalized and normalized {@link F}
	 */
	private F project(F phi, Separator<F> S) {
		int[] ints = Arrays.stream(phi.getDomain().getVariables())
				.filter(x -> !ArraysUtil.contains(x, S.getVariables()))
				.toArray();
		return phi.marginalize(ints);
	}
}
