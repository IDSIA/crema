package ch.idsia.crema.inference.bp;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.bp.cliques.Clique;
import ch.idsia.crema.inference.bp.junction.JunctionTree;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.preprocess.Observe;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.idsia.crema.utility.ArraysUtil.difference;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:03
 */
public class BeliefPropagation<F extends OperableFactor<F>> implements Inference<DAGModel<F>, F> {

	protected DAGModel<F> model;
	protected DirectedAcyclicGraph<Clique, DefaultEdge> collectingTree;
	protected DirectedAcyclicGraph<Clique, DefaultEdge> distributingTree;

	protected LinkedList<Clique> collectingOrder;
	protected LinkedList<Clique> distributionOrder;

	protected Clique root;

	protected TIntIntMap evidence;

	protected final Map<Clique, Set<F>> potentialsPerClique = new HashMap<>();
	protected final Map<Pair<Clique, Clique>, F> messages = new HashMap<>();
	protected final Map<Pair<Clique, Clique>, int[]> separators = new HashMap<>();

	protected Boolean preprocess = true;
	protected Boolean fullyPropagated = false;

	public BeliefPropagation() {
	}

	public BeliefPropagation(Boolean preprocess) {
		setPreprocess(preprocess);
	}

	public void setPreprocess(Boolean preprocess) {
		this.preprocess = preprocess;
	}

	public Boolean isFullyPropagated() {
		return fullyPropagated;
	}

	/**
	 * If {@link #preprocess} is true, then pre-process the model with {@link Observe} and {@link RemoveBarren}.
	 *
	 * @param original the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the pre-processed model
	 */
	protected DAGModel<F> preprocess(DAGModel<F> original, TIntIntMap evidence, int... query) {
		DAGModel<F> model = original;
		if (preprocess) {
			model = original.copy();
			final Observe<F> co = new Observe<>();
			final RemoveBarren<F> rb = new RemoveBarren<>();

			co.executeInPlace(model, evidence);
			rb.executeInPlace(model, evidence, query);
		}

		return model;
	}

	/**
	 * Uses the {@link GraphToJunctionTreePipe} to build a {@link JunctionTree} that will be then used to build the
	 * {@link #collectingTree} and {@link #distributingTree}. The trees are stored inside this model until the next time
	 * this method is invoked.
	 *
	 * @param model the model to use for inference
	 * @param query the variable that will be queried
	 */
	protected void initModel(DAGModel<F> model, int query) {
		this.fullyPropagated = false;
		this.model = model;

		GraphToJunctionTreePipe<F> pipeline = new GraphToJunctionTreePipe<>();
		pipeline.setInput(model);
		JunctionTree junctionTree = pipeline.exec();

		potentialsPerClique.clear();
		messages.clear();

		junctionTree.vertexSet().forEach(clique -> potentialsPerClique.put(clique, new HashSet<>()));

		junctionTree.vertexSet().forEach(clique -> {
			for (int v : clique.getVArray()) {
				final F f = model.getFactor(v);
				potentialsPerClique.get(clique).add(f);
			}
		});

		collectingTree = new DirectedAcyclicGraph<>(DefaultEdge.class);
		distributingTree = new DirectedAcyclicGraph<>(DefaultEdge.class);

		collectingOrder = new LinkedList<>();
		distributionOrder = new LinkedList<>();

		junctionTree.vertexSet().forEach(clique -> {
			collectingTree.addVertex(clique);
			distributingTree.addVertex(clique);
		});

		final Set<Clique> visited = new HashSet<>();
		root = getRoot(junctionTree, query);

		Set<Clique> nodes = new HashSet<>();
		nodes.add(root);

		// add the edges to the two directed trees
		do {
			Set<Clique> slack = new HashSet<>();
			for (Clique clique : nodes) {
				visited.add(clique);
				// TODO: maybe using a single order then reverse?
				collectingOrder.addFirst(clique);
				distributionOrder.addLast(clique);
				for (DefaultEdge edge : junctionTree.edgesOf(clique)) {
					final Clique source = junctionTree.getEdgeSource(edge);
					final Clique target = junctionTree.getEdgeTarget(edge);
					if (!visited.contains(source)) {
						collectingTree.addEdge(source, clique);
						distributingTree.addEdge(clique, source);
						slack.add(source);
					}
					if (!visited.contains(target)) {
						collectingTree.addEdge(target, clique);
						distributingTree.addEdge(clique, target);
						slack.add(target);
					}
					separators.put(
							new ImmutablePair<>(source, target),
							difference(source.getVariables(), target.getVariables())
					);
					separators.put(
							new ImmutablePair<>(target, source),
							difference(target.getVariables(), source.getVariables())
					);
				}
			}

			nodes = slack;
		} while (!nodes.isEmpty());
	}

	protected Clique getRoot(JunctionTree junctionTree, int variable) {
		return junctionTree.vertexSet()
				.stream()
				.filter(c -> c.contains(variable))
				.min(Comparator.comparingInt(c -> c.getVariables().length))
				.orElseThrow(() -> new IllegalArgumentException("Variable " + variable + " not found in model"));
	}

	/**
	 * Pre-process the model with {@link Observe} and {@link RemoveBarren}, then performs the
	 * {@link #collectingEvidence(int)} step.
	 *
	 * @param model the model to use for inference
	 * @param query the variable that will be queried
	 * @return the marginal probability of the query variable
	 */
	@Override
	public F query(DAGModel<F> model, int query) {
		return query(model, new TIntIntHashMap(), query);
	}

	/**
	 * Pre-process the model with {@link Observe} and {@link RemoveBarren}, then performs the
	 * {@link #collectingEvidence(int)} step.
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
		initModel(model, query);

		return collectingEvidence(query);
	}

	/**
	 * Pre-process the model with {@link Observe} and {@link RemoveBarren}, then performs the
	 * {@link #collectingEvidence(int)} and {@link #distributingEvidence()} steps.
	 * <p>
	 * Use the {@link #queryFullPropagated(int)} method for query multiple variables over the same evidence and model.
	 *
	 * @param model the model to use for inference
	 * @param query the variable that will be queried
	 * @return the marginal probability of the query variable
	 */
	public F fullPropagation(DAGModel<F> model, int query) {
		return fullPropagation(model, new TIntIntHashMap(), query);
	}

	/**
	 * Performs the {@link #collectingEvidence(int)} and {@link #distributingEvidence()} steps. No pre-processing is
	 * applied.
	 * <p>
	 * Use the {@link #queryFullPropagated(int)} method for query multiple variables over the same evidence and model.
	 *
	 * @param model    the model to use for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param query    the variable that will be queried
	 * @return the marginal probability of the query variable
	 */
	public F fullPropagation(DAGModel<F> model, TIntIntMap evidence, int query) {
		setPreprocess(false);
		F f = query(model, evidence, query);
		distributingEvidence();
		fullyPropagated = true;

		return f;
	}

	/**
	 * Before use this method, it is mandatory to user the {@link #fullPropagation(DAGModel, TIntIntMap, int)} method.
	 *
	 * @param query the variable to query
	 * @return the marginal probability of the query variable
	 */
	public F queryFullPropagated(int query) {
		if (!fullyPropagated)
			throw new IllegalStateException("The stored model is not fully propagated.");

		final Clique i = collectingTree.vertexSet()
				.stream()
				.filter(c -> c.contains(query))
				.min(Comparator.comparingInt(c -> c.getVariables().length))
				.orElseThrow(() -> new IllegalArgumentException("Variable " + query + " not found in model"));

		final List<Clique> parents = parents(collectingTree, i);
		final List<Clique> children = children(collectingTree, i);

		final Optional<F> Ms = Stream.concat(parents.stream(), children.stream())
				.map(j -> new ImmutablePair<>(j, i))
				.map(messages::get)
				.reduce(F::combine);

		F f = phi(i);
		if (Ms.isPresent())
			f = f.combine(Ms.get());

		int[] ints = difference(f.getDomain().getVariables(), new int[]{query});
		return f.marginalize(ints).normalize();
	}

	/**
	 * @param i the {@link Clique} to search the parents for
	 * @return all the parents of the given {@link Clique} in the {@link #collectingTree}
	 */
	protected List<Clique> parents(DirectedAcyclicGraph<Clique, DefaultEdge> tree, Clique i) {
		return tree.incomingEdgesOf(i)
				.stream()
				.map(tree::getEdgeSource)
				.collect(Collectors.toList());
	}

	/**
	 * @param i the {@link Clique} to search the children for
	 * @return all the children of the given {@link Clique} in the {@link #collectingTree}
	 */
	protected List<Clique> children(DirectedAcyclicGraph<Clique, DefaultEdge> tree, Clique i) {
		return tree.outgoingEdgesOf(i)
				.stream()
				.map(tree::getEdgeTarget)
				.collect(Collectors.toList());
	}

	/**
	 * @param tree the tree to use
	 * @param i    the {@link Clique} source of the message
	 * @return the combination of the potentials of {@link Clique} i with all the incoming messages (if there are any)
	 */
	protected F message(DirectedAcyclicGraph<Clique, DefaultEdge> tree, Clique i) {
		final Optional<F> psis = parents(tree, i)
				.stream()
				.map(j -> {
					final ImmutablePair<Clique, Clique> key = new ImmutablePair<>(j, i);
					final F M = messages.get(key);
					final int[] S = separators.get(key);
					return M.marginalize(S).normalize();
				})
				.reduce(F::combine);

		F phis = phi(i);

		if (psis.isPresent())
			phis = phis.combine(psis.get());

		return phis.normalize();
	}

	/**
	 * @param i {@link Clique} to collect messages to
	 * @return the combination of the potentials of {@link Clique} i with all the incoming messages (if there are any)
	 */
	protected F collect(Clique i) {
		return message(collectingTree, i);
	}

	/**
	 * @param i {@link Clique} to distribute messages from
	 * @return the combination of the potentials of {@link Clique} i with all the incoming messages (if there are any)
	 */
	protected F distribute(Clique i) {
		return message(distributingTree, i);
	}

	/**
	 * Executes the collecting step of the Belief Propagation algorithm.
	 *
	 * @param variable the variable to query as root of the tree
	 * @return the precise {@link ch.idsia.crema.factor.GenericFactor} associated with the variable
	 */
	public F collectingEvidence(int variable) {
		// populate messages
		for (Clique i : collectingOrder) {
			// root computation is done outside of this loop
			if (i.equals(root))
				continue;

			// in collectingTree, children is always 1 (except for root)
			final Clique j = children(collectingTree, i).get(0);
			final F Mij = collect(i);
			final Pair<Clique, Clique> key = new ImmutablePair<>(i, j);
			messages.put(key, Mij);
		}

		// root computation and variable query
		final F f = collect(root);
		int[] ints = difference(f.getDomain().getVariables(), new int[]{variable});
		return f.marginalize(ints).normalize();
	}

	/**
	 * Executes the distribution step of the Belief Propagation algorithm.
	 */
	public void distributingEvidence() {
		for (Clique i : distributionOrder) {
			final F Mi = distribute(i);

			for (Clique j : children(distributingTree, i)) {
				final Pair<Clique, Clique> key = new ImmutablePair<>(i, j);
				final int[] S = separators.get(key);
				final F Mij = Mi.marginalize(S);
				messages.put(key, Mij);
			}
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
