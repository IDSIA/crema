package ch.idsia.crema.model.causal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map.Entry;
import java.util.NoSuchElementException;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class SCM extends DAGModel<BayesianFactor> {

	public static enum VariableType {
		/** Endgogenous variable have at least one exogenous counfounder */
		ENDOGENOUS, 
		
		/** Exogenous variables are root nodes with no parents */
		EXOGENOUS, 
		
		/** Extra variables are endogenous variables without direct exogenous influence */
		EXTRA
	}
	
	private int nextId;
	private TreeMap<VariableType, IntSet> varSets;

	private TreeMap<String, Object> metadata;

	public SCM() {
		varSets = new TreeMap<SCM.VariableType, IntSet>();
		
		varSets.put(VariableType.ENDOGENOUS, new IntOpenHashSet());
		varSets.put(VariableType.EXOGENOUS, new IntOpenHashSet());
		varSets.put(VariableType.EXTRA, new IntOpenHashSet());
		
		metadata = new TreeMap<String, Object>();
	}
	
	public SCM(SCM copy) {
		super(copy);
		metadata = new TreeMap<String, Object>(copy.metadata);
		
		varSets = new TreeMap<SCM.VariableType, IntSet>();

		varSets.put(VariableType.ENDOGENOUS, new IntOpenHashSet(copy.varSets.get(VariableType.ENDOGENOUS)));
		varSets.put(VariableType.EXOGENOUS, new IntOpenHashSet(copy.varSets.get(VariableType.EXOGENOUS)));
		varSets.put(VariableType.EXTRA, new IntOpenHashSet(copy.varSets.get(VariableType.EXTRA)));
		
	}
	
	
	public void setMetadata(String key, Object value) {
		metadata.put(key, value);
	}
	
	public Object getMetadata(String key) {
		return metadata.get(key);
	}
	
	public void copyMetadataTo(SCM target) {
		target.metadata.putAll(metadata);
	}
	
	
	public SCM clone() {
		return new SCM(this);
	}
	
	public SCM copy() {
		return new SCM(this);
	}
	
	private int nextId() { 
		return nextId;
	}
	
		
	private void useId(int id) {
		nextId = Math.max(nextId, id + 1);
	}
	
	public void add(TypedVariable variable) {
		this.add(variable.getLabel(), variable.getCardinality(), variable.getType());
	}
	
	public int add(int size, VariableType type) {
		int varid = nextId();
		return this.add(varid, size, type);
	}
	
	public int add(int variable, int size, VariableType type) {
		if (super.cardinalities.containsKey(variable))
			return variable;
		
		useId(variable);
		
		super.addVariable(variable, size);
		varSets.get(type).add(variable);
		
		return variable;
	}

	public int addEndogenous(int size) {
		return this.add(size, VariableType.ENDOGENOUS);
	}

	public int addExogenous(int size) {
		return this.add(size, VariableType.EXOGENOUS);
	}

	public int addExtra(int size) {
		return this.add(size, VariableType.EXTRA);
	}

	public int addEndogenous(int variable, int size) {
		return this.add(variable, size, VariableType.ENDOGENOUS);
	}

	public int addExogenous(int variable, int size) {
		return this.add(variable, size, VariableType.EXOGENOUS);
	}

	public int addAdditional(int variable, int size) {
		return this.add(variable, size, VariableType.EXOGENOUS);
	}

	public boolean isEndogenous(int variable) {
		return varSets.get(VariableType.ENDOGENOUS).contains(variable);
	}

	public boolean isExogenous(int variable) {
		return varSets.get(VariableType.EXOGENOUS).contains(variable);
	}

	public boolean isExtra(int variable) {
		return varSets.get(VariableType.EXTRA).contains(variable);
	}

	public VariableType getVariableType(int v) {
		for (var set : varSets.entrySet()) { 
			if (set.getValue().contains(v)) return set.getKey();
		}
		return null;
	}
	
	public void setVariableType(int v, VariableType newtype) {
		VariableType type = getVariableType(v);
		// remove
		varSets.get(type).remove(v);
		// add
		varSets.get(newtype).add(v);
	}

	
	public boolean has(int variable) {
		return this.cardinalities.containsKey(variable);
	}
	
	public VariableType getType(int variable) { 
		for (VariableType type : VariableType.values()) {
			if (varSets.get(type).contains(variable)) return type;
		}
		return null;
	}
	
	public int[] getVariables(VariableType... types) {
		IntSet vars = new IntOpenHashSet();
		for (var type : types) {
			vars.addAll(varSets.get(type));
		}
		return vars.toIntArray();
	}
	
	
	public IntSet getEndogenousSet() {
		return varSets.get(VariableType.ENDOGENOUS);
	}
	
	public IntSet getExogenousSet() {
		return varSets.get(VariableType.EXOGENOUS);
	}
	
	public IntSet getExtraSet() {
		return varSets.get(VariableType.EXTRA);
	}
	
	public int[] getEndogenous() {
		return getEndogenousSet().toIntArray();
	}

	public int[] getExogenous() {
		return getExogenousSet().toIntArray();
	}

	public int[] getExtra() {
		return getExtraSet().toIntArray();
	}

	public void addParent(int variable, int parent) {
		if (isExogenous(variable))
			throw new IllegalStateException("Exogenous vars have no parents.");

		if (isExtra(variable) && isExogenous(parent))
			throw new IllegalStateException("Additional vars cannot depend on exogenous vars directly.");

		if (isExtra(variable) && !isExtra(parent))
			throw new IllegalStateException("Additional vars should only depend on each other.");

		super.addParent(variable, parent);
	}

	public Iterable<TypedVariable> variables() {
		return () -> {
			return new Iterator<TypedVariable>() {
				int type_index = 0;
				VariableType[] types = Arrays.stream(VariableType.values()).filter(x -> varSets.get(x).size() > 0).toArray(VariableType[]::new);
			
				IntIterator setIterator = varSets.get(types[0]).intIterator();
				
				@Override
				public TypedVariable next() {
					if (!setIterator.hasNext()) {
						++type_index;
						if (type_index < types.length) {
							setIterator = varSets.get(types[type_index]).iterator();
						} else {
							throw new NoSuchElementException();
						}
					}
					int id = setIterator.nextInt();
					int size = cardinalities.get(id);
					return new TypedVariable(id, size, types[type_index]);
				}
				
				@Override
				public boolean hasNext() {
					boolean setn = setIterator.hasNext();
					boolean typn = type_index < types.length - 1;
					
					return setn || typn; // either set has more or we're not in the last set
				}
			};
		};
	}
	
	@Override
	public void removeVariable(int variable) {
		super.removeVariable(variable);

		// remove from sets (if needed)
		@SuppressWarnings("unused")
		boolean changed = this.varSets.get(VariableType.ENDOGENOUS).remove(variable)
				|| this.varSets.get(VariableType.EXOGENOUS).remove(variable)
				|| this.varSets.get(VariableType.EXTRA).remove(variable);
	}

	
	
	
	
}
