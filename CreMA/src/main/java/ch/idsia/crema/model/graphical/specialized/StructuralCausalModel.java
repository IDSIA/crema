package ch.idsia.crema.model.graphical.specialized;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.BayesianToVertex;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.GenericSparseModel;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.primitives.Ints;

import java.util.HashSet;
import java.util.Set;

/**
 * Author:  Rafael Cabañas
 * Date:    04.02.2020
 * <p>
 * A Structural Causal Model is a special type of {@link GenericSparseModel}, composed with {@link BayesianFactor} and
 * constructed on a {@link SparseDirectedAcyclicGraph}. Differs from Bayesian networks on having 2 different
 * kind of variables: exogenous and endogenous
 */
public class StructuralCausalModel extends GenericSparseModel<BayesianFactor, SparseDirectedAcyclicGraph> {

	/**
	 * Create the directed model using the specified network implementation.
	 */
	public StructuralCausalModel() {
		super(new SparseDirectedAcyclicGraph());
	}

	private Set<Integer> exogenousVars = new HashSet<Integer>();
	
	public StructuralCausalModel copy(){

		StructuralCausalModel copy = new StructuralCausalModel();
		copy.addVariables(this.getSizes(this.getVariables()));
		for (int v : copy.getVariables()){
			copy.addParents(v, this.getParents(v));
			copy.setFactor(v, this.getFactor(v).copy());
		}

		return copy;
	}

	public int addVariable(int size, boolean exogenous) {
		int vid = super.addVariable(size);
		if(exogenous)
			this.exogenousVars.add(vid);
		return vid;
	}


	@Override
	public void removeVariable(int variable) {
		super.removeVariable(variable);
		if(exogenousVars.contains(variable))
			exogenousVars.remove(variable);

	}

	public int[] getExogenousVars() {
		return Ints.toArray(exogenousVars);
	}

	public boolean isExogenous(int variable){
		return exogenousVars.contains(variable);
	}

	public boolean isEndogenous(int variable) {
		return !isExogenous(variable);
	}

	public int[] getEndogenousVars() {
		Set<Integer> endogenousVars = new HashSet<Integer>();

		for(int v : this.getVariables())
			if(!this.exogenousVars.contains(v))
				endogenousVars.add(v)	;
		return Ints.toArray(endogenousVars);
	}


	public static StructuralCausalModel getCausalStructFromBN(BayesianNetwork bnet, int... exoVarSizes){

		if(exoVarSizes.length!= 1 && exoVarSizes.length != bnet.getVariables().length)
			throw new IllegalArgumentException("exoVarSizes vector should be a vector of lenght 1 or as long as the number of variables");

		StructuralCausalModel smodel = new StructuralCausalModel();
		smodel.addVariables(bnet.getSizes(bnet.getVariables()));

		for (int i = 0; i<bnet.getVariables().length; i++){
			int v = bnet.getVariables()[i];
			smodel.addParents(v, bnet.getParents(v));
			if(exoVarSizes.length==1)
				smodel.addParent(v, smodel.addVariable(exoVarSizes[0], true));
			else
				smodel.addParent(v, smodel.addVariable(exoVarSizes[i], true));
		}

		return smodel;
	}



	public SparseModel toVertexSimple(BayesianNetwork empirical_model){

		//todo: check that evidence model is correct and that this case can be applied

		// Copy the structure of the this
		SparseModel cmodel = new SparseModel();
		cmodel.addVariables(this.getSizes(this.getVariables()));
		for (int v : cmodel.getVariables()){
			cmodel.addParents(v, this.getParents(v));
		}


		// Set the credal sets for the endogenous variables X
		for(int v: this.getEndogenousVars()) {
			VertexFactor kv = new BayesianToVertex().apply(this.getFactor(v), 0);
			cmodel.setFactor(v, kv);
		}

		// Get the credal sets for the exogenous variables U
		for(int v: this.getExogenousVars()) {
			double [] vector = this.getFactor(this.getChildren(v)[0]).getData();


			double[][] coeff = ArraysUtil.transpose(ArraysUtil.reshape2d(
					this.getFactor(this.getChildren(v)[0]).getData(), this.getSizes(v)
			));

			double[] vals = empirical_model.getFactor(this.getChildren(v)[0]).getData();

			VertexFactor kv = new VertexFactor(cmodel.getDomain(v), coeff, vals);
			cmodel.setFactor(v, kv);
		}

		return cmodel;
	}


	public int[] getExogenousParents(int v){
		return ArraysUtil.intersection(
				this.getExogenousVars(),
				this.getParents(v)
		);
	}

	public void fillWithRandomFactors(int prob_decimals){

		for(int u : this.getExogenousVars()){
			this.setFactor(u,
					BayesianFactor.random(this.getDomain(u),
							this.getDomain(this.getParents(u)),
							prob_decimals, false)
			);
		}


		for(int x : this.getEndogenousVars()){
			Strides pa_x = this.getDomain(this.getParents(x));
			int[] assignments = RandomUtil.sampleUniform(pa_x.getCombinations(),this.getSize(x), true);

			this.setFactor(x,
					BayesianFactor.deterministic(
							this.getDomain(x),
							pa_x,
							assignments)
			);
		}


	}


	public BayesianFactor getProb(int var) {
		BayesianFactor pvar = this.getFactor(var);
		for (int v : this.getExogenousParents(var)) {
			pvar = pvar.combine(this.getFactor(v));
		}
		for (int v : this.getExogenousParents(var)) {
			pvar = pvar.marginalize(v);
		}
		return pvar;
	}


	public StructuralCausalModel intervention(int var, int state){
		StructuralCausalModel do_model = this.copy();
		// remove the parents
		for(int v: do_model.getParents(var)){
			do_model.removeParent(var, v);
		}
		// fix the value
		do_model.setFactor(var, BayesianFactor.deterministic(do_model.getDomain(var), state));

		return do_model;

	}

}
