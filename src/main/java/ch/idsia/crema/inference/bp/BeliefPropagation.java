package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.junction.JunctionTree;
import ch.idsia.crema.inference.bp.junction.Separator;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.idsia.crema.utility.ArraysUtil.difference;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:03
 */
public class BeliefPropagation<F extends Factor<F>> implements Inference<DAGModel<F>, F> {

	protected DAGModel<F> model;
	protected JunctionTree<F> junctionTree;

	protected TIntIntMap evidence = new TIntIntHashMap();

	protected final Map<Clique, Set<F>> potentialsPerClique = new HashMap<>();

	protected Boolean preprocess = true;

	public void setPreprocess(Boolean preprocess) {
		this.preprocess = preprocess;
	}

	protected DAGModel<F> preprocess(DAGModel<F> original, TIntIntMap evidence, int... query) {
		DAGModel<F> model = original;
		if (preprocess) {
			model = original.copy();
			final CutObserved<F> co = new CutObserved<>();
			final RemoveBarren<F> rb = new RemoveBarren<>();

			co.executeInPlace(model, evidence);
			rb.executeInPlace(model, evidence, query);
		}

		return model;
	}

	protected void initModel(DAGModel<F> model) {
		this.model = model;

		GraphToJunctionTreePipe<F> pipeline = new GraphToJunctionTreePipe<>();
		pipeline.setInput(model.getNetwork());
		junctionTree = pipeline.exec();

		// when we assign a potential to a clique, we put this clique at the end of the list
		final LinkedList<Clique> cliques = junctionTree.vertexSet().stream()
				.sorted(Comparator.comparingInt(a -> -junctionTree.edgesOf(a).size()))
				.collect(Collectors.toCollection(LinkedList::new));

		// distribute potentials
		for (F factor : model.getFactors()) {
			final int[] dom = factor.getDomain().getVariables();

			for (Clique clique : cliques) {
				if (clique.getVariables().length < dom.length)
					continue;

				if (clique.containsAll(dom)) {
					potentialsPerClique.computeIfAbsent(clique, i -> new HashSet<>()).add(factor);
					cliques.remove(clique);
					cliques.addLast(clique);
					break;
				}
			}
		}
	}

	public JunctionTree<F> getJunctionTree() {
		return junctionTree;
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

	@Override
	public F query(DAGModel<F> model, int query) {
		return query(model, new TIntIntHashMap(), query);
	}

	/**
	 * Pre-process the model with {@link CutObserved} and {@link RemoveBarren}.
	 *
	 * @param original the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the marginal probability of the query variable
	 */
	@Override
	public F query(DAGModel<F> original, TIntIntMap evidence, int query) {
		model = preprocess(original, evidence, query);

		this.evidence = evidence;
		initModel(model);

		return collectingEvidence(query);
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

		F phis = phi(root);

		if (junctionTree.vertexSet().size() > 1) {
			// collect messages
			F psis = junctionTree.edgesOf(root).stream()
					.map(edge -> cliqueFromDirection(root, edge))
					.map(i -> collect(/* from */ i, /* to */root))
					.reduce(F::combine)
					.orElseThrow(() -> new IllegalStateException("No factor after combination with messages"));

			// combine by potential
			phis = psis.combine(phis).normalize();
		}

		// marginalize out what is not needed
		int[] ints = difference(phis.getDomain().getVariables(), new int[]{variable});
		return phis.marginalize(ints).normalize();
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
				.orElseThrow(() -> new IllegalStateException("Empty F after combination"))
				.normalize();
	}

}
