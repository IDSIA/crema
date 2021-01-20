package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.junction.JunctionTree;
import ch.idsia.crema.inference.bp.junction.Separator;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

		if (fullyPropagated) {
			f = phi(getRoot(variable));
		} else {
			f = collectingEvidence(variable);
		}

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

		Clique root = getRoot(variable);

		// collect messages
		Stream<F> psis = junctionTree.edgesOf(root).stream()
				.map(edge -> {
					Clique source = junctionTree.getEdgeSource(edge);
					Clique target = junctionTree.getEdgeTarget(edge);

					Clique i = root == source ? target : source;

					return collect(/* from */ i, /* to */root);
				});

		Stream<F> phis = Stream.of(phi(root));

		// combine by potential
		F f = Stream.concat(phis, psis)
				.reduce(F::combine)
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
		F phi = phi(root);

		junctionTree.outgoingEdgesOf(root).forEach(edge -> {
			Clique source = junctionTree.getEdgeSource(edge);
			Clique target = junctionTree.getEdgeTarget(edge);

			Clique i = root == source ? target : source;

			Separator<F> S = junctionTree.getEdge(i, root);
			F Mij = project(phi, S);

			// distribute the message M(root,i) to node i
			distribute(root, i, Mij);
		});

		fullyPropagated = true;
	}

	private void checks() {
		if (model == null) throw new IllegalStateException("No network available");
		if (junctionTree == null) throw new IllegalStateException("No JunctionTree available");
	}

	/**
	 * @param j from this node
	 * @param k to this node
	 * @return the message Mjk
	 */
	private F collect(Clique j, Clique k) {
		// collect phi(j)
		F phi = phi(j);

		for (Separator<F> edge : junctionTree.edgesOf(j)) {
			Clique source = junctionTree.getEdgeSource(edge);
			Clique target = junctionTree.getEdgeTarget(edge);

			// if we have inbound edges for this node j, we compute the message
			Clique i = j == source ? target : source;

			// ignore messages inbound from k
			if (i.equals(k)) continue;

			F Mij = collect(i, j);

			phi.combine(Mij);
		}

		// compute the message by projecting phi over the separator(i, j)
		Separator<F> S = junctionTree.getEdge(j, k);
		F Mjk = project(phi, S);
		S.setMessage(j, Mjk);

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
			Clique source = junctionTree.getEdgeSource(edge);
			Clique target = junctionTree.getEdgeTarget(edge);

			Clique j = i == source ? target : source;

			// ignore message outgoing to k
			if (j.equals(k)) continue;

			F Mij = project(phi, junctionTree.getEdge(i, j));

			distribute(i, j, Mij);
		}
	}

	/**
	 * Compute the potential phi of a given {@link Clique}. Evidence is considered there.
	 *
	 * @param clique input clique
	 * @return a {@link F} which is a combination of all the factors and the evidences included in the Clique
	 */
	private F phi(Clique clique) {
		F factor = IntStream.of(clique.getVariables())
				.mapToObj(model::getFactor)
				.reduce(F::combine)
				.orElseThrow(() -> new IllegalStateException("Empty F after combination"));

		return factor.filter(evidence).normalize();
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

		return phi.marginalize(ints).normalize();
	}
}
