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

	private Clique cliqueFromDirection(Clique from, Separator<F> edge) {
		Clique source = junctionTree.getEdgeSource(edge);
		Clique target = junctionTree.getEdgeTarget(edge);

		return from == source ? target : source;
	}

	public int[] variablesNotInSeparator(Clique c, Separator<F> S) {
		return Arrays.stream(c.getVariables())
				.filter(x -> !ArraysUtil.contains(x, S.getVariables()))
				.toArray();
	}

	private void checks() {
		if (model == null) throw new IllegalStateException("No network available");
		if (junctionTree == null) throw new IllegalStateException("No JunctionTree available");
	}

	/**
	 * @param variable variable to query
	 * @return the marginal probability of the given query
	 */
	public F query(int variable) {
		F f;

		if (fullyPropagated) {
			f = queryFullyPropagated(variable);
		} else {
			f = collectingEvidence(variable);
		}

		return f;
	}

	public F query(int variable, TIntIntMap evidence) {
		setEvidence(evidence);
		return query(variable);
	}

	public F queryFullyPropagated(int variable) {
		// TODO: implement other smart ways to query a fully propagated junction tree
		final Clique root = getRoot(variable);
		final F phis = phi(root);

		final F psis = junctionTree.edgesOf(root).stream()
				.map(edge -> {
					Clique source = junctionTree.getEdgeSource(edge);
					Clique target = junctionTree.getEdgeTarget(edge);

					Clique i = root == source ? target : source;

					return edge.getMessage(i);
				})
				.reduce(F::combine)
				.orElseThrow(() -> new IllegalStateException("No factor after combination with messages"));

		F f = psis.combine(phis);

		// marginalize out what is not needed
		int[] ints = IntStream.of(f.getDomain().getVariables()).filter(x -> x != variable).toArray();

		return f.marginalize(ints).normalize();
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
		int[] ints = variablesNotInSeparator(j, S);
		F Mjk;

		if (psis.isPresent()) {
			Mjk = phis.combine(psis.get()).marginalize(ints);
		} else {
			Mjk = phis.marginalize(ints);
		}

		S.setMessage(/* from */ j, Mjk);

//		System.out.println("PSI(" + j + " -> " + k + ":" + S + ") = " + Mjk);

		return Mjk;
	}

	/**
	 * Executes the distribution step of the Belief Propagation algorithm.
	 *
	 * @param variable the variable to consider the root node
	 */
	public void distributingEvidence(int variable) {
		checks();

		Clique root = getRoot(variable);
		F phis = phi(root);

		junctionTree.outgoingEdgesOf(root).stream()
				.map(edge -> cliqueFromDirection(root, edge))
				.forEach(i -> {
					Separator<F> S = junctionTree.getEdge(i, root);
					int[] ints = variablesNotInSeparator(root, S);
					F Mij = phis.marginalize(ints);
					// distribute the message from root to i
					S.setMessage(root, Mij);
					distribute(root, i);
				});

		fullyPropagated = true;
	}

	/**
	 * This method is called for each node k from the node j. This will collect all messages from other edges, excluded
	 * edge k->next, and update the message of the k->next edge. This update is done for all edges. Then the method call
	 * an update on all following 'next' nodes.
	 *
	 * @param j from this node
	 * @param k to this node
	 */
	private void distribute(Clique j, Clique k) {
		F phis = phi(k);

		// if we have nodes that need the message from this node k
		for (Separator<F> edgeTo : junctionTree.edgesOf(k)) {
			Clique target = cliqueFromDirection(k, edgeTo); // target that will receive the message

			// ignore message outgoing to j
			if (target.equals(j)) continue;

			List<F> messages = new ArrayList<>();

			// collect messages from other edges
			for (Separator<F> edgeFrom : junctionTree.edgesOf(k)) {
				Clique source = cliqueFromDirection(k, edgeFrom); // source will give the message

				// don't get message from where we want to update
				if (source.equals(target))
					continue;

				messages.add(edgeFrom.getMessage(source));
			}

			Optional<F> psis = messages.stream().reduce(F::combine);
			int[] ints = variablesNotInSeparator(j, edgeTo);
			F M;

			if (psis.isPresent()) {
				M = phis.combine(psis.get()).marginalize(ints);
			} else {
				M = phis.marginalize(ints);
			}

			edgeTo.setMessage(k, M);
		}

		// proceed with distribution
		for (Separator<F> edgeTo : junctionTree.edgesOf(k)) {
			Clique i = cliqueFromDirection(k, edgeTo);

			// ignore message outgoing to j
			if (i.equals(j)) continue;

			distribute(k, i);
		}
	}

	/**
	 * Compute the potential phi of a given {@link Clique}. Evidence is considered there.
	 *
	 * @param clique input clique
	 * @return a {@link F} which is a combination of all the factors and the evidences included in the Clique
	 */
	private F phi(Clique clique) {
		return potentialsPerClique.get(clique).stream()
				.map(f -> f.filter(evidence))
				.reduce(F::combine)
				.orElseThrow(() -> new IllegalStateException("Empty F after combination"));

//		System.out.println("PHI(" + clique + ") = " + phi);
//		return phi;
	}
}
