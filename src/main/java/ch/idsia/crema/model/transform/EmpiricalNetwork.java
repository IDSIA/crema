package ch.idsia.crema.model.transform;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.IteratorUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Lists;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.data.DoubleTable;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.ve.order.TopologicalOrdering;
import ch.idsia.crema.model.causal.SCM;
import ch.idsia.crema.model.causal.SCM.VariableType;
import ch.idsia.crema.model.graphical.BayesianNetwork;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class EmpiricalNetwork
//implements BiFunction<SCM, DoubleTable, BayesianNetwork>
{
	
//	
//	
//	
//	public BayesianNetwork apply(SCM model, DoubleTable data) {
//		BayesianNetwork bn = new BayesianNetwork();
//		
//		var comps = components(model);
//		
//		IntList topo_order = topological(model);
//		
//		for (int variable: topo_order) {
//			bn.addVariable(variable, model.getSize(variable));
//		}
//		
//		for (int i = 0; i < topo_order.size(); ++i) {
//			int variable = topo_order.getInt(i);
//			
//			// topologically before 
//			var topo_before = Arrays.stream(topo_order, 0, i);
//			var friends = comps.get(variable);
//			
//			// get all nodes strictly topologically before variable and that are part of the component
//			int[] parents = topo_before.filter(friends::contains).toArray();
//			bn.addParents(variable, parents);
//		}
//		
//		quantify(bn, data);
//
//		return bn;
//	}
//	
//	
//
//	public double loglikelihood(BayesianNetwork net, DoubleTable dataset) {
//		
//		int[] vars = net.getVariables();
//		
//		double ll = 0;
//		for (var row : dataset) {
//			int[] states = row.getKey();
//			double rowll = 0;
//			for(int variable : vars) {
//				var factor = net.getFactor(variable);
//				int offset = factor.getDomain().getPartialOffset(dataset.getColumns(), states);
//				double p = factor.getValueAt(offset);
//				rowll += Math.log(p);
//			}
//			ll += rowll * row.getValue();
//		}
//		return ll;
//	}
//
//	
//	
//	private void quantify(BayesianNetwork bn, DoubleTable data, int[] variables) {
//
//		for (int variable : variables) {
//			int[] parents = bn.getParents(variable);
//			
//			int[] target = new int[parents.length + 1];
//			System.arraycopy(parents, 0, target, 0, parents.length);
//			target[parents.length] = variable;
//			Arrays.sort(target);
//			
//			Strides domain = bn.getDomain(target);
//			double[] values = data.getWeights(domain.getVariables(), domain.getSizes());
//			
//			BayesianFactor factor = new BayesianFactor(domain, values, false);
//			factor = factor.normalize(parents);
//			
//			bn.setFactor(variable, factor);
//		}
//	}
//	
//	
//	private Int2ObjectMap<IntSet> components(SCM model) {
//	
//		List<IntSet> components = new LinkedList<IntSet>();
//		
//		IntSet todo = new IntOpenHashSet(model.getVariables());
//		
//		IntSet exogenous = new IntOpenHashSet(model.getExogenousSet());
//		IntSet exo_close = new IntOpenHashSet();
//		while (!exogenous.isEmpty()) {
//			int exo = exogenous.iterator().next();
//			exogenous.remove(exo);
//			
//			// already processed
//			if(exo_close.contains(exo)) continue;
//			exo_close.add(exo);
//			
//			// visit connected components
//			IntSet component = new IntOpenHashSet(model.getEndogenousChildren(exo));
//			components.add(component);
//
//			IntSet open = new IntOpenHashSet(component);
//			while(!open.isEmpty()) {
//				int child = open.iterator().next();
//				open.remove(child);
//				
//				IntSet exo_parents = new IntOpenHashSet(model.getExogenousParents(child));
//				exo_parents.removeAll(exo_close);
//				
//				// we are processing the exogenous parents
//				exogenous.removeAll(exo_parents);
//				exo_close.addAll(exo_parents);
//				
//				// add to the open variables all the children of the exogenous 
//				for (int connected_exo_var : exo_parents) {
//					
//					// for each non closed exo var 
//					// add all children to open
//					IntSet siblings = model.getChildrenSet(connected_exo_var);
//					// remove already processed children
//					siblings.removeAll(component);
//					open.addAll(siblings);
//					
//					component.addAll(siblings);
//				}
//			}
//			
//			// add all parents of endogenous variables
//			int[] compvars = component.toIntArray();
//			for (int variable : compvars) {
//				var parents = model.getParentsSet(variable);
//				parents.removeAll(model.getExogenousSet());
//				component.addAll(parents);
//			}
//		}
//		
//
//		// assign sets to all variables
//		Int2ObjectMap<IntSet> sets = new Int2ObjectOpenHashMap<IntSet>();
//		
//		for (IntSet component : components) {
//			
//			
//		
//		}
//		return sets;
//	}
//	
//	
//	/**
//	 * get the topological ordering of endogenous vars
//	 * @param model
//	 * @return
//	 */
//	public IntList topological(SCM model) {
//		IntSet exo = model.getExogenousSet();
//		
//		TopologicalOrderIterator<Integer, DefaultEdge> iter = new TopologicalOrderIterator<Integer, DefaultEdge>(model.getNetwork());
//		IntList order = new IntArrayList(model.getVariables().length);
//		while(iter.hasNext()) {
//			order.add(iter.next());
//		}
//		order.removeAll(exo);
//		return order;
//	}
}
