package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static ch.idsia.crema.utility.ArraysUtil.outersection;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.03.2021 17:35
 */
public class LoopyBeliefPropagation<F extends Factor<F>> implements Inference<DAGModel<F>, F> {

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

	protected DAGModel<F> model;

	protected DirectedAcyclicGraph<Integer, DefaultEdge> network;
	protected SimpleGraph<Integer, DefaultEdge> graph;

	protected Boolean preprocess = true;

	protected int iterations = 5;

	protected Map<Pair<Integer, Integer>, F> messages;
	protected Map<Pair<Integer, Integer>, Neighbour> neighbours;

	public LoopyBeliefPropagation() {
	}

	public LoopyBeliefPropagation(int iterations) {
		setIterations(iterations);
	}

	public LoopyBeliefPropagation(Boolean preprocess) {
		setPreprocess(preprocess);
	}

	public LoopyBeliefPropagation(Boolean preprocess, int iterations) {
		setPreprocess(preprocess);
		setIterations(iterations);
	}

	/**
	 * @param iterations max number of iterations to do until convergence
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

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
		// TODO check if this work has already been done!
		this.model = model;
		network = model.getNetwork();
		graph = new SimpleGraph<>(DefaultEdge.class);
		messages = new HashMap<>();
		neighbours = new HashMap<>();

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

		// initialize all messages with the same value of the current node i
		for (Pair<Integer, Integer> key : neighbours.keySet()) {
			final int[] vars = neighbours.get(key).variables;
			final F fi = model.getFactor(key.getLeft());
			final int[] ints_i = outersection(fi.getDomain().getVariables(), vars);
			messages.put(key, fi.marginalize(ints_i).normalize());
		}
	}

	/**
	 * @param i    source node
	 * @param j    destination node
	 * @param vars variables shared between source and destination node
	 */
	protected void addMailbox(Integer i, Integer j, int[] vars) {
		final Neighbour nij = new Neighbour(i, j, vars);
		final Pair<Integer, Integer> key_ij = new ImmutablePair<>(i, j); // (i, j)

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
	 * @param model the model to use for inference
	 * @param query the variable that will be queried
	 * @return the marginal probability of the given query
	 */
	@Override
	public F query(DAGModel<F> model, int query) {
		return query(model, new TIntIntHashMap(), query);
	}

	/**
	 * @param original the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the marginal probability of the given query
	 */
	@Override
	public F query(DAGModel<F> original, TIntIntMap evidence, int query) {
		final DAGModel<F> model = preprocess(original, evidence, query);

		initModel(model);
		messagePassing(evidence);
		return variableMarginal(query);
	}

	protected void sendMessage(int i, int j, DAGModel<F> model, TIntIntMap evidence, Map<Pair<Integer, Integer>, F> new_messages) {
		// send message from i to j
		final Pair<Integer, Integer> key = new ImmutablePair<>(i, j); // (i, j)
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

		final int[] ints = outersection(f.getDomain().getVariables(), neighbour.variables);
		Mij = Mij.marginalize(ints).normalize();

		if (Mij.getDomain().getSize() == 0)
			throw new IllegalStateException("Message defined over any variable");

		new_messages.put(key, Mij);
	}

	protected void messagePassing(TIntIntMap evidence) {
		// iterate and update messages
		for (int it = 0; it < iterations; it++) {
			final Map<Pair<Integer, Integer>, F> new_messages = new HashMap<>();

			for (int node : model.getVariables()) {
				final int[] parents = model.getParents(node);
				final int[] children = model.getChildren(node);

				for (int parent : parents)
					sendMessage(node, parent, model, evidence, new_messages);
				for (int child : children)
					sendMessage(node, child, model, evidence, new_messages);
			}

			// update all messages and go to the next iteration
			messages.putAll(new_messages);
			// TODO: add check to stop when we converged (new_messages are equal to old_messages)
		}
	}

	private F variableMarginal(int query) {
		F v = model.getFactor(query);
		if (!graph.edgeSet().isEmpty()) {
			final F M = graph.edgesOf(query).stream()
					.map(edge -> {
						final Integer s = graph.getEdgeSource(edge);
						final Integer t = graph.getEdgeTarget(edge);
						return s.equals(query) ? t : s;
					})
					.map(s -> new ImmutablePair<>(s, query))
					.map(messages::get)
					.reduce(F::combine)
					.orElseThrow(() -> new IllegalStateException("Empty F after message combination"));

			v = v.combine(M);
		}

		int[] ints = IntStream.of(v.getDomain().getVariables()).filter(x -> x != query).toArray();
		return v.marginalize(ints).normalize();
	}

}
