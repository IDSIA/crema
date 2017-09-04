package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.NoSuchVariableException;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.change.CardinalityChange;
import ch.idsia.crema.model.change.DomainChange;
import ch.idsia.crema.model.change.NullChange;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * A graphical model that will not update indices when a variable is deleted.
 * This is the way to go!!!!
 * 
 * @author david
 */
@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.FIELD)
public class SparseModel<F extends GenericFactor> implements GraphicalModel<F> {

	@XmlTransient
	private DomainChange<F> domainChanger;

	@XmlTransient
	private CardinalityChange<F> cardinalityChanger;

	@XmlTransient
	private int max = 0;

	private Graph network;

	/**
	 * The number of states/cardinalities of a variable
	 */
	private TIntIntMap cardinalities;

	/**
	 * The factor associated to a variable
	 */
	private TIntObjectMap<F> factors;

	/**
	 * Create a directed graphical model based on a {@link SparseList} graph
	 * model.
	 */
	public SparseModel() {
		this(new SparseList()); // list based graph model
	}

	/**
	 * Create the directed model using the specified network implementation.
	 * 
	 * @param method
	 */
	public SparseModel(Graph method) {
		network = method;
		cardinalities = new TIntIntHashMap();
		factors = new TIntObjectHashMap<>();

		NullChange<F> changer = new NullChange<>();

		domainChanger = changer;
		cardinalityChanger = changer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SparseModel<F> copy() {
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
		return this.convert((f,v)->(F) f.copy());
	}

	/**
	 * Make a copy of the network while transforming the factors. All variable
	 * labels/ID will remain the same.
	 * 
	 * @param converter
	 *            a BiFunction that will take the source factor, the variable
	 *            and return a new factor
	 * @return the new model
	 */
	public <R extends GenericFactor> SparseModel<R> convert(BiFunction<F, Integer, R> converter) {
		NullChange<R> changer = new NullChange<>();

		Graph graph = network.copy();
		SparseModel<R> new_model = new SparseModel<>(graph);
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
		int index = Arrays.binarySearch(vars, variable);

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

	public F[] getFactors(F[] example) {
		return factors.values(example);
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

}
