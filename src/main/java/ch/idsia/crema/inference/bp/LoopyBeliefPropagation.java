package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.InferenceCascade;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.03.2021 17:35
 */
public class LoopyBeliefPropagation<F extends Factor<F>> implements Inference<DAGModel<F>, F>, InferenceCascade<DAGModel<F>, F> {

	protected static class Neighbour {
		final Integer i;
		final Integer j;
		final List<Integer> incoming = new ArrayList<>();
		final int[] variables;

		public Neighbour(Integer i, Integer j, int[] variables) {
			this.i = i;
			this.j = j;
			this.variables = variables;
		}
	}

	protected DAGModel<F> original;
	protected DAGModel<F> model;

	protected DirectedAcyclicGraph<Integer, DefaultEdge> network;
	final SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

	protected int iterations = 5;

	protected final Map<ImmutablePair<Integer, Integer>, F> messages = new HashMap<>();
	protected final Map<ImmutablePair<Integer, Integer>, Neighbour> neighbours = new HashMap<>();

	private TIntIntMap evidence = new TIntIntHashMap();
	protected Boolean updated = false;

	@Override
	public void setModel(DAGModel<F> model) {
		// TODO check if this work has already been done!
		this.model = model;
		this.network = model.getNetwork();

		// copy network into an undirected (simple) graph
		// add all the vertices to the new graph
		network.vertexSet().forEach(graph::addVertex);

		// add all the edges to the new graph
		network.vertexSet().forEach(v -> {
			for (DefaultEdge e1 : network.incomingEdgesOf(v)) {
				int parent = network.getEdgeSource(e1);
				graph.addEdge(parent, v);
			}
		});

		// build mailboxes
		for (Integer i : graph.vertexSet()) {
			for (Integer j : graph.vertexSet()) {
				if (model.getNetwork().getEdge(i, j) == null)
					// this edge does not exist
					continue;

				final F fi = model.getFactor(i);
				final F fj = model.getFactor(j);

				final int[] vars = ArraysUtil.intersection(fi.getDomain().getVariables(), fj.getDomain().getVariables());

				addMailbox(i, j, vars);
				addMailbox(j, i, vars);
			}
		}
	}

	// TODO: perform pre-processing!
	@Override
	public void setModel(DAGModel<F> model, TIntIntMap evidence) {
		setEvidence(evidence);
		setModel(model);
	}

	// TODO: perform pre-processing!
	@Override
	public void setEvidence(TIntIntMap evidence) {
		this.evidence = evidence;
	}

	/**
	 * @param i    source node
	 * @param j    destination node
	 * @param vars variables shared between source and destination node
	 */
	private void addMailbox(Integer i, Integer j, int[] vars) {
		final Neighbour nij = new Neighbour(i, j, vars);
		final ImmutablePair<Integer, Integer> key_ij = new ImmutablePair<>(i, j); // (i, j)

		for (DefaultEdge e : graph.edgesOf(i)) {
			final Integer s = graph.getEdgeSource(e);
			final Integer t = graph.getEdgeTarget(e);

			final Integer target = t.equals(i) ? s : t;

			if (!target.equals(j))
				nij.incoming.add(target);
		}
		neighbours.put(key_ij, nij);
	}

	/**
	 * @param iterations max number of iterations to do until convergence
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
		this.updated = false;
	}

	protected int[] outerIntersection(int[] vars, int[] sep) {
		return Arrays.stream(vars)
				.filter(x -> !ArraysUtil.contains(x, sep))
				.toArray();
	}

	private void checkEvidence(TIntIntMap evidence) {
		if (this.evidence != null && !this.evidence.equals(evidence)) {
			this.evidence = evidence;
			this.updated = false;
		}
	}

	/**
	 * This method can be used to run multiple query with the same model structure.
	 *
	 * @param variable variable to query
	 * @return the marginal probability of the given query
	 */
	@Override
	public F query(int variable) {
		final F v = model.getFactor(variable);

		checkEvidence(evidence);

		if (!updated)
			messagePassing(evidence);

		return variableMarginal(variable, v);
	}

	private void messagePassing(TIntIntMap evidence) {
		// initialize all messages with the same value of the current node i
		for (ImmutablePair<Integer, Integer> key : neighbours.keySet()) {
			final int[] vars = neighbours.get(key).variables;
			final F fi = model.getFactor(key.getLeft());
			final int[] ints_i = outerIntersection(fi.getDomain().getVariables(), vars);
			messages.put(key, fi.marginalize(ints_i).normalize());
		}

		// iterate and update messages
		for (int it = 0; it < iterations; it++) {
			final Map<ImmutablePair<Integer, Integer>, F> new_messages = new HashMap<>();

			for (Integer i : network) { // xi
				for (Integer j : network) { // xj
					if (i.equals(j))
						// impossible edges
						continue;

					if (graph.getEdge(i, j) == null)
						// non existing edges
						continue;

					// send message from i to j
					final ImmutablePair<Integer, Integer> key = new ImmutablePair<>(i, j); // (i, j)
					final Neighbour neighbour = neighbours.get(key);
					final F f = model.getFactor(i);

					F Mij = f; // gi(xi)

					if (!neighbour.incoming.isEmpty()) {
						// collect messages and propagate: PROD Mki_old(xi)
						F Mki = neighbour.incoming.stream()
								.map(k -> new ImmutablePair<>(k, i))
								.map(messages::get)
								.reduce(F::combine)
								.orElseThrow(() -> new IllegalStateException("Empty F after combination"));

						Mij = Mij.combine(Mki).normalize(); // h(xi) = gi(xi) * PROD Mki_old(xi)
					}

					if (evidence.containsKey(i)) {
						// propagate evidence
						Mij = Mij.filter(evidence);
					}

					final int[] ints = outerIntersection(f.getDomain().getVariables(), neighbour.variables);
					Mij = Mij.marginalize(ints).normalize();

					if (Mij.getDomain().getSize() == 0)
						throw new IllegalStateException("Message defined over any variable");

					new_messages.put(key, Mij);
				}
			}

			// update all messages and go to the next iteration
			messages.putAll(new_messages);
			// TODO: add check to stop when we converged (new_messages are equal to old_messages)
		}

		updated = true;
	}

	private F variableMarginal(int variable, F v) {
		final F M = graph.edgesOf(variable).stream()
				.map(edge -> {
					final Integer s = graph.getEdgeSource(edge);
					final Integer t = graph.getEdgeTarget(edge);

					return s.equals(variable) ? t : s;
				})
				.map(s -> new ImmutablePair<>(s, variable))
				.map(messages::get)
				.reduce(F::combine)
				.orElseThrow(() -> new IllegalStateException("Empty F after message combination"));

		final F f = v.combine(M);
		int[] ints = IntStream.of(f.getDomain().getVariables()).filter(x -> x != variable).toArray();

		return f.marginalize(ints).normalize();
	}

	/**
	 * @param model    the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the marginal probability of the given query
	 */
	@Override
	public F query(DAGModel<F> model, TIntIntMap evidence, int query) {
		setModel(model);
		setEvidence(evidence);

		final CutObserved<F> co = new CutObserved<>();
		final RemoveBarren<F> rb = new RemoveBarren<>();

		final GraphicalModel<F> cutted = co.execute(model, evidence);
		final DAGModel<F> removed = (DAGModel<F>) rb.execute(cutted, evidence, query);

		setModel(removed);

		return query(query);
	}

	/**
	 *
	 * @param model the model to use for inference
	 * @param query the variable that will be queried
	 * @return
	 */
	@Override
	public F query(DAGModel<F> model, int query) {
		return query(model, new TIntIntHashMap(), query);
	}

}
