package ch.idsia.crema.model.causal.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.model.causal.SCM;
import ch.idsia.crema.model.causal.SCM.VariableType;
import ch.idsia.crema.model.causal.WorldModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class TreeMapping implements WorldModel {
	
	/** 
	 * Mapping from source to target for each world id
	 * Key is world id, value is mapping between source and global id
	 */
	private TIntObjectMap<TIntIntMap> toGlobal;
	
	/**
	 * A map from a global id to a pair <world id, node id>
	 */
	private TIntObjectMap<Pair<Integer, Integer>> fromGlobal;
	
	/**
	 * A map from global exogenous id to local exogenous id
	 */
	private TIntIntMap globalToLocalExogenous;
	private TIntIntMap localToGlobalExogenous;
	
	/**
	 * the global model, initialized at the first add.
	 */
	private SCM global;
	
	/**
	 * list of source models who's id are the index in this list
	 */
	private List<SCM> worlds;

	/**
	 * Create a new Tree Mapping object.
	 */
	public TreeMapping() {
		this.toGlobal = new TIntObjectHashMap<TIntIntMap>();
		this.fromGlobal = new TIntObjectHashMap<Pair<Integer,Integer>>();
		this.globalToLocalExogenous = new TIntIntHashMap();
		this.localToGlobalExogenous= new TIntIntHashMap();
		this.worlds = new ArrayList<SCM>();
	}
	
	
	@Override
	public SCM get() {
		return global;
	}

	/**
	 * Store the mapping for the given world
	 * @param wid world id
	 * @param translate a map from local to global
	 */
	protected void saveMapping(int wid, TIntIntMap translate) {
		
		toGlobal.put(wid, translate);
		
		var iter = translate.iterator();
		while(iter.hasNext()) {
			iter.advance();
			var source = iter.key();
			var target = iter.value();
			if (localToGlobalExogenous.containsKey(source)) continue;
			
			fromGlobal.put(target, Pair.of(wid, source));
		}
	}
	
	/**
	 * Rename a list of source variable to the target
	 * @param ids
	 * @param translate
	 * @return
	 */
	private int[] rename(int[] ids, TIntIntMap translate) {
		int[] target = new int[ids.length];
		for (int i = 0; i < ids.length; ++i) {
			target[i] = translate.get(ids[i]);
		}
		return target;
//		return Arrays.stream(ids).map(translate::get).toArray();
	}
	
	/** 
	 * Convert a bayesian factor to a new variables set
	 * @param factor
	 * @param translate
	 * @return
	 */
	private BayesianFactor rename(BayesianFactor factor, TIntIntMap translate) {
		Strides domain = factor.getDomain();
		int[] newvars = Arrays.stream(domain.getVariables()).map(translate::get).toArray();
		
		int[] order = ArraysUtil.order(newvars);
		double[] source_data = factor.getData();

//		int[] source = domain.getVariables().clone();
//		source = ArraysUtil.at(source, order);
//		
//		var iterator = domain.getReorderedIterator(source);
//		int tid = 0;
//		
//		double[] target = new double[domain.getCombinations()];
//		
//		while(iterator.hasNext()) {
//			target[tid++] = source_data[iterator.next()];
//		}
		
		int[] target_vars = ArraysUtil.at(newvars, order);
		int[] target_size = ArraysUtil.at(domain.getSizes(), order);
		
		return BayesianFactorFactory.factory().domain(target_vars, target_size, true).data(newvars, source_data).get();
	}
	
	/**
	 * Connect a new model to the global one. 
	 */
	@Override
	public int add(SCM model) {
		int wid = worlds.size();
		worlds.add(model);
		
		if (global == null) {
			global = new SCM();
		}
		
		TIntIntMap translate = new TIntIntHashMap();
		for (var source : model.variables()) {
			if (source.getType() == VariableType.EXOGENOUS) {
				if (!localToGlobalExogenous.containsKey(source.getLabel())) {
					int id = global.add(source.getCardinality(), source.getType());
					localToGlobalExogenous.put(source.getLabel(), id);
					globalToLocalExogenous.put(id, source.getLabel());
					translate.put(source.getLabel(), id);
				} else {
					int id = localToGlobalExogenous.get(source.getLabel());
					translate.put(source.getLabel(), id);
				}
			} else {
				int id = global.add(source.getCardinality(), source.getType());
				translate.put(source.getLabel(), id);
			}
		}
		
		
		for (var source : model.variables()) {
			if(source.getType() == VariableType.EXOGENOUS) continue;
			int[] parents = model.getParents(source.getLabel());
			parents = rename(parents, translate);
			
			int globalId = translate.get(source.getLabel());
			global.addParents(globalId, parents);
		}
		
		for (var source : model.variables()) {
			var factor = model.getFactor(source.getLabel());
			if (factor == null) continue;
			
			factor = rename(factor, translate);
			int tid = translate.get(source.getLabel());
			global.setFactor(tid, factor);
		}
		
		saveMapping(wid, translate);
		return wid;
	}


	@Override
	public int toGlobal(int variable, int world) {		
		return toGlobal.get(world).get(variable);
	}

	@Override
	public int fromGlobal(int variable) {
		if (globalToLocalExogenous.containsKey(variable)) {
			return globalToLocalExogenous.get(variable);
		}
		var p = fromGlobal.get(variable);
		return p.getValue();
	}

	@Override
	public int worldIdOf(int variable) {
		if (globalToLocalExogenous.containsKey(variable)) return -1;
		var p = fromGlobal.get(variable);
		return p.getKey();
	}

	@Override
	public SCM worldOf(int variable) {
		if (globalToLocalExogenous.containsKey(variable)) return null;
		return worlds.get(worldIdOf(variable));
	}
	
}
