package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.NoSuchVariableException;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.change.CardinalityChange;
import ch.idsia.crema.model.change.DomainChange;
import ch.idsia.crema.model.change.NullChange;
import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Ints;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 10:48
 */
public class GenericGraphicalModel<F extends GenericFactor, G extends Graph> implements GraphicalModel<F> {


	@XmlTransient
	protected DomainChange<F> domainChanger;

	@XmlTransient
	protected CardinalityChange<F> cardinalityChanger;

	@XmlTransient
	protected int max = 0;

	protected G network;

	/**
	 * The number of states/cardinalities of a variable
	 */
	protected TIntIntMap cardinalities;

	/**
	 * The factor associated to a variable
	 */
	protected TIntObjectMap<F> factors;

	/**
	 * Create the directed model using the specified network implementation.
	 *
	 * @param network the network implementation to use
	 */
	public GenericGraphicalModel(G network) {
		this.network = network;
		cardinalities = new TIntIntHashMap();
		factors = new TIntObjectHashMap<>();

		NullChange<F> changer = new NullChange<>();

		domainChanger = changer;
		cardinalityChanger = changer;
	}

	public G getNetwork() {
		return network;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenericGraphicalModel<F, G> copy() {
//		Graph graph = network.copy();
//		SparseModel<F> new_model = new SparseModel<>(graph);
//		new_model.domainChanger = domainChanger;
//		new_model.cardinalityChanger = cardinalityChanger;
//
//		new_model.cardinalities = new TIntIntHashMap(this.cardinalities);
//		new_model.factors = new TIntObjectHashMap<>(factors.size());
//		new_model.max = this.max;
//
//		TIntObjectIterator<F> iterator = this.factors.iterator();
//		while (iterator.hasNext()) {
//			iterator.advance();
//			new_model.factors.put(iterator.key(), (F) iterator.value().copy());
//		}
//		return new_model;

		// lets use convert to make the copy
		return this.convert((f, v) -> (F) f.copy());
	}

	/**
	 * Make a copy of the network while transforming the factors. All variable
	 * labels/ID will remain the same.
	 *
	 * @param converter a BiFunction that will take the source factor, the variable
	 *                  and return a new factor
	 * @return the new model
	 */
	@SuppressWarnings("unchecked")
	public <R extends GenericFactor> GenericGraphicalModel<R, G> convert(BiFunction<F, Integer, R> converter) {
		NullChange<R> changer = new NullChange<>();

		Graph graph = network.copy();
		GenericGraphicalModel<R, G> new_model = new GenericGraphicalModel<>((G) graph);
		new_model.domainChanger = changer;
		new_model.cardinalityChanger = changer;

		new_model.cardinalities = new TIntIntHashMap(this.cardinalities);
		new_model.factors = new TIntObjectHashMap<>(factors.size());
		new_model.max = this.max;

		TIntObjectIterator<F> iterator = this.factors.iterator();
		while (iterator.hasNext()) {
			iterator.advance();
			R new_factor = converter.apply(iterator.value(), iterator.key());
			new_model.factors.put(iterator.key(), new_factor);
		}

		return new_model;
	}

	@Override
	public void addState(int variable) {
		F factor = factors.get(variable);
		F new_factor = cardinalityChanger.addState(factor, variable);
		if (factor != new_factor)
			factors.put(variable, new_factor);

		for (int child : getChildren(variable)) {
			factor = factors.get(child);
			new_factor = cardinalityChanger.addParentState(factor, child, variable);
			if (factor != new_factor)
				factors.put(child, new_factor);
		}

		// save the change
		this.cardinalities.put(variable, this.cardinalities.get(variable) + 1);
	}

	@Override
	public void removeState(int variable, int state) {
		F factor = factors.get(variable);
		F new_factor = cardinalityChanger.removeState(factor, variable, state);
		if (factor != new_factor)
			factors.put(variable, new_factor);

		// save the change
		this.cardinalities.put(variable, this.cardinalities.get(variable) - 1);
	}

	@Override
	public int getVariablesCount() {
		return cardinalities.size();
	}

	@Override
	public int getSize(int variable) {
		return this.cardinalities.get(variable);
	}

	@Override
	public int[] getSizes(int... variables) {
		int[] sizes = new int[variables.length];
		int pos = 0;
		for (int variable : variables) {
			sizes[pos++] = this.cardinalities.get(variable);
		}
		return sizes;
	}

	@Override
	public void removeVariable(int variable) {
		int[] children = getChildren(variable);

		// update the factors
		for (int child : children) {
			F factor = factors.get(child);
			F new_factor = domainChanger.remove(factor, variable);
			if (factor != new_factor)
				factors.put(variable, new_factor);
		}

		cardinalities.remove(variable);
		factors.remove(variable);
		network.removeVariable(variable);
	}

	@Override
	public int addVariable(int size) {
		int vid = max++;
		this.cardinalities.put(vid, size);
		network.addVariable(vid, size);
		return vid;
	}

	public int addVariable(int vid, int size) {
		if (vid > max) max = vid;
		max++;
		this.cardinalities.put(vid, size);
		network.addVariable(vid, size);
		return vid;
	}

	@Override
	public void removeParent(int variable, int parent) {
		F factor = factors.get(variable);
		F new_factor = domainChanger.remove(factor, parent);
		if (factor != new_factor)
			factors.put(variable, new_factor);
		network.removeLink(parent, variable);
	}

	@Override
	public void removeParent(int variable, int parent, DomainChange<F> change) {
		F factor = factors.get(variable);
		F new_factor = change.remove(factor, parent);
		if (factor != new_factor)
			factors.put(variable, new_factor);
		network.removeLink(parent, variable);
	}

	@Override
	public void addParent(int variable, int parent) {
		F factor = factors.get(variable);
		F new_factor = domainChanger.add(factor, parent);
		if (factor != new_factor)
			factors.put(variable, new_factor);
		network.addLink(parent, variable);
	}

	@Override
	public int[] getParents(int variable) {
		return network.getParents(variable);
	}

	@Override
	public int[] getChildren(int variable) {
		return network.getChildren(variable);
	}

	@Override
	public int[] getRoots() {
		int[] variables = getVariables();

		// TODO: stream or TIntArray?
//		return Arrays.stream(variables).filter(v -> getParents(v).length == 0).toArray();

		TIntArrayList list = new TIntArrayList();
		for (int variable : variables) {
			if (getParents(variable).length == 0)
				list.add(variable);
		}

		return list.toArray();
	}

	@Override
	public int[] getLeaves() {
		int[] variables = getVariables();

		// TODO: see TODO in method getRoots()
		// Arrays.stream(variables).filter(v -> getChildren(v).length == 0).toArray()

		TIntArrayList list = new TIntArrayList();
		for (int variable : variables) {
			if (getChildren(variable).length == 0)
				list.add(variable);
		}

		return list.toArray();
	}

	@Override
	public Strides getDomain(int... variables) {
		return new Strides(variables, getSizes(variables));
	}

	public void addVariables(int... states) {
		for (int size : states) {
			addVariable(size);
		}
	}

	public void addParents(int variable, int... parents) {
		for (int parent : parents) {
			addParent(variable, parent);
		}
	}

	@Override
	public F getFactor(int variable) {
		return factors.get(variable);
	}

	@Override
	public void setFactor(int variable, F factor) {
		int[] vars = factor.getDomain().getVariables();
		int index = ArrayUtils.indexOf(vars, variable);
		int[] parents = ArraysUtil.remove(vars, index);

		addParents(variable, parents);

		factors.put(variable, factor);
	}

	@Override
	public int[] getVariables() {
		int[] vars = cardinalities.keys();
		Arrays.sort(vars);
		return vars;
	}

	@Override
	public Collection<F> getFactors() {
		return factors.valueCollection();
	}

	public Collection<F> getFactors(int... variables) {
		return IntStream.of(variables).mapToObj(v -> factors.get(v)).collect(Collectors.toList());
	}

	@Override
	public void setFactors(F[] factors) {
		if (factors.length != getVariablesCount()) {
			throw new IllegalArgumentException("This model requires one factor per variable");
		}

		int index = 0;
		for (int var : getVariables()) {
			F factor = factors[index++];
			if (factor != null && !factor.getDomain().contains(var)) {
				throw new NoSuchVariableException(var, factor.getDomain());
			}

			this.factors.put(var, factor);
		}
	}

	@SuppressWarnings("unchecked")
	public GenericGraphicalModel<F, G> observe(int var, int state) {
		GenericGraphicalModel<F, G> observedModel = this.copy();
		// Fix the value of the intervened variable
		observedModel.setFactor(var, (F) this.getFactor(var).getDeterministic(var, state));
		return observedModel;
	}

	/**
	 * Determines if the factor domains match with the structure of the DAG.
	 *
	 * @return
	 */
	public boolean correctFactorDomains() {
		return IntStream.of(this.getVariables())
				.allMatch(v -> Arrays.equals(
						ArraysUtil.sort(this.getFactor(v).getDomain().getVariables()),
						ArraysUtil.sort(ArrayUtils.add(this.getParents(v), v))
				));
	}

	/**
	 * Determines if the factor domains match with the structure of the DAG.
	 *
	 * @return true if the network is correct, otherwise false
	 */
	public boolean inspectFactorDomains() {
		boolean correct = true;

		for (int v : this.getVariables()) {
			int[] d1 = ArraysUtil.sort(this.getFactor(v).getDomain().getVariables());
			int[] d2 = ArraysUtil.sort(ArrayUtils.add(this.getParents(v), v));

			if (!Arrays.equals(d1, d2)) {
				System.out.println("Error in " + v + ":");
				System.out.println("factor domain: " + Arrays.toString(d1));
				System.out.println("factor in net:  " + Arrays.toString(d2));
				correct = false;
			}
		}

		return correct;
	}

	/**
	 * Returns an array with the parents of a variable and the variable itself.
	 *
	 * @param variable source variable
	 * @return an array with all the parents of the variable, then the variable itself
	 */
	public int[] getVariableAndParents(int variable) {
		return ArrayUtils.add(this.getParents(variable), variable);
	}

	/**
	 * Returns an array with the isolated variables (without parents and children)
	 *
	 * @return an array of all the isolated variables
	 */
	public int[] getDisconnected() {
		return IntStream.of(this.getVariables())
				.filter(v -> this.getParents(v).length == 0 && this.getChildren(v).length == 0)
				.toArray();
	}

	/**
	 * Check if there is a path (without considering the link directions) between 2 nodes.
	 *
	 * @param node1 start node
	 * @param node2 end node
	 * @return true if there exist a path between the two nodes, otherwise false
	 */
	public boolean areConnected(int node1, int node2) {
		return areConnected(node1, node2, new int[]{});
	}

	private boolean areConnected(int node1, int node2, int[] visited) {
		int[] neighbours = Ints.concat(this.getChildren(node1), this.getParents(node1));

		if (Ints.asList(neighbours).contains(node2))
			return true;

		for (int v : neighbours)
			if (!Ints.asList(visited).contains(v) && this.areConnected(v, node2, Ints.concat(visited, new int[]{node1})))
				return true;

		return false;
	}

	public int[] getDescendants(int variable) {
		int[] ch = getChildren(variable);

		if (ch.length == 0)
			return ch;

		int[] desc_ch = Ints.concat(IntStream.of(ch).mapToObj(this::getDescendants).toArray(int[][]::new));
		return ArraysUtil.unique(Ints.concat(ch, desc_ch));
	}

	public int[] getAncestors(int variable) {
		int[] pa = getParents(variable);

		if (pa.length == 0)
			return pa;

		int[] ances_pa = Ints.concat(IntStream.of(pa).mapToObj(this::getAncestors).toArray(int[][]::new));
		return ArraysUtil.unique(Ints.concat(pa, ances_pa));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GSM\n");

		for (F f : this.getFactors()) {
			sb.append("\t").append(f.toString()).append("\n");
		}

		return sb.toString();
	}

}
