package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.stream.Stream;

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

	protected final DAGModel<F> model;
	protected final DirectedAcyclicGraph<Integer, DefaultEdge> network;

	protected int iterations = 5;

	protected final Map<ImmutablePair<Integer, Integer>, F> messages = new HashMap<>();
	protected final Map<ImmutablePair<Integer, Integer>, Neighbour> neighbours = new HashMap<>();

	public LoopyBeliefPropagation(DAGModel<F> model) {
		this.model = model;
		this.network = model.getNetwork();
		init();
	}

	protected void init() {
		for (Integer v : network) {
			// iter in topological order
			final Set<DefaultEdge> edges = network.edgesOf(v);
			final F f = model.getFactor(v);

			for (DefaultEdge edge : edges) {
				final Integer a = network.getEdgeSource(edge);
				final Integer b = network.getEdgeTarget(edge);

				final Integer i = a.equals(v) ? a : b;
				final Integer j = b.equals(v) ? a : b;

				final F fi = model.getFactor(i);
				final F fj = model.getFactor(j);
				final int[] vars = ArraysUtil.intersectionSorted(fi.getDomain().getVariables(), fj.getDomain().getVariables());

				// message from i to j
				final ImmutablePair<Integer, Integer> key = new ImmutablePair<>(i, j);

				// build the neighbourhood: all the incoming edges that are not equals to the target j node
				Neighbour n = new Neighbour(i, j, vars);

				for (DefaultEdge e : edges) {
					final Integer s = network.getEdgeSource(e);
					final Integer t = network.getEdgeTarget(e);

					final Integer target = t.equals(i) ? s : t;

					if (!target.equals(j))
						n.incoming.add(target);
				}

				neighbours.put(key, n);

				// init all messages with the same value of the current node i
				final int[] ints = variablesNotInSeparator(f.getDomain().getVariables(), n.variables);
				messages.put(key, f.marginalize(ints));
			}
		}
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public int[] variablesNotInSeparator(int[] vars, int[] sep) {
		return Arrays.stream(vars)
				.filter(x -> !ArraysUtil.contains(x, sep))
				.toArray();
	}

	@Override
	public DAGModel<F> getInferenceModel(int target, TIntIntMap evidence) {
		return null;
	}

	@Override
	public F query(int variable, TIntIntMap evidence) {

		if (evidence.containsKey(variable)) {
			return model.getFactor(variable).filter(evidence).normalize();
		}

		final Map<ImmutablePair<Integer, Integer>, F> new_messages = new HashMap<>();

		// iterate and update messages
		for (int it = 0; it < iterations; it++) {
			// iter in topological order
			for (Integer i : network) {
				for (Integer j : network) {
					if (i.equals(j))
						// impossible edges
						continue;

					if (model.getNetwork().getEdge(i, j) == null)
						// this edge does not exist
						continue;

					// send message to i from j
					final ImmutablePair<Integer, Integer> key = new ImmutablePair<>(i, j);
					final Neighbour neighbour = neighbours.get(key);
					final F f = model.getFactor(i);

					F Mij;

					if (evidence.containsKey(i)) {
						// propagate evidence
						Mij = f.filter(evidence);

					} else if (neighbour.incoming.isEmpty()) {
						Mij = f;

					} else {
						// collect messages and propagate
						Mij = Stream.concat(
								Stream.of(f),
								neighbour.incoming.stream()
										.map(k -> new ImmutablePair<>(k, i))
										.map(messages::get)
						)
								.reduce(Factor::combine)
								.orElseThrow(() -> new IllegalStateException("Empty F after combination"))
								.normalize();

					}

					final int[] ints = variablesNotInSeparator(f.getDomain().getVariables(), neighbour.variables);
					Mij = Mij.marginalize(ints);

					new_messages.put(key, Mij);
				}
			}

			// update all messages and go to the next iteration
			messages.putAll(new_messages);
		}

		final F M = network.edgesOf(variable).stream()
				.map(edge -> {
					final Integer s = network.getEdgeSource(edge);
					final Integer t = network.getEdgeTarget(edge);

					return s.equals(variable) ? t : s;
				})
				.map(s -> new ImmutablePair<>(variable, s))
				.map(messages::get)
				.reduce(Factor::combine)
				.orElseThrow(() -> new IllegalStateException("Empty F after message combination"));

		return model.getFactor(variable).combine(M).normalize();
	}
}
