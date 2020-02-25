package ch.idsia.crema.model.graphical.specialized;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.BayesianToVertex;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.counterfact.CounterFactMapping;
import ch.idsia.crema.model.graphical.GenericSparseModel;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Author:  Rafael Cabañas
 * Date:    04.02.2020
 * <p>
 * A Structural Causal Model is a special type of {@link GenericSparseModel}, composed with {@link BayesianFactor} and
 * constructed on a {@link SparseDirectedAcyclicGraph}. Differs from Bayesian networks on having 2 different
 * kind of variables: exogenous and endogenous
 */
public class StructuralCausalModel extends GenericSparseModel<BayesianFactor, SparseDirectedAcyclicGraph> {


	/** mapping of each variables to other counterfactual scenarios. In case of a non counterfactual setting
	 * this will be null*/
	private CounterFactMapping map;

	private String name="";

	/** set of variables that are exogenous. The rest are considered to be endogenous */
	private Set<Integer> exogenousVars = new HashSet<Integer>();



	/**
	 * Create the directed model using the specified network implementation.
	 */
	public StructuralCausalModel() {
		super(new SparseDirectedAcyclicGraph());
	}

	public StructuralCausalModel(String name) {
		super(new SparseDirectedAcyclicGraph());
		this.name=name;
	}

	/**
	 * Builds a simple SCM from a empirical DAG such that each each endogenous variable (i.e., those from in the DAG)
	 * has a single exogenous variable.
	 * @param empiricalDAG
	 * @param endoVarSizes
	 * @param exoVarSizes
	 */
	public StructuralCausalModel(SparseDirectedAcyclicGraph empiricalDAG, int[] endoVarSizes, int... exoVarSizes){
		super(new SparseDirectedAcyclicGraph());

		if(endoVarSizes.length != empiricalDAG.getVariables().length)
			throw new IllegalArgumentException("endoVarSizes vector should as long as the number of vertices in the dag");

		Strides dagDomain = new Strides(empiricalDAG.getVariables(), endoVarSizes);

		if(exoVarSizes.length==0){
			exoVarSizes =  IntStream.of(empiricalDAG.getVariables())
					.map(v -> dagDomain.intersection(ArrayUtils.add(empiricalDAG.getParents(v), v)).getCombinations()+1)
					.toArray();
		}else if(exoVarSizes.length==1){
			int s = exoVarSizes[0];
			exoVarSizes = IntStream.range(0,empiricalDAG.getVariables().length).map(i-> s).toArray();
		}


		this.addVariables(dagDomain.getSizes());

		for (int i = 0; i<empiricalDAG.getVariables().length; i++){
			int v = empiricalDAG.getVariables()[i];
			this.addParents(v, empiricalDAG.getParents(v));
			if(exoVarSizes.length==1)
				this.addParent(v, this.addVariable(exoVarSizes[0], true));
			else
				this.addParent(v, this.addVariable(exoVarSizes[i], true));
		}

	}



	/**
	 * Create a copy of this model (i.e. dag and factors are copied)
	 * @return
	 */
	public StructuralCausalModel copy(){

		StructuralCausalModel copy = new StructuralCausalModel();
		for (int v: this.getVariables()){
			copy.addVariable(this.getSize(v), this.isExogenous(v));
		}

		for (int v : copy.getVariables()){
			copy.addParents(v, this.getParents(v));
			if(this.getFactor(v)!=null)
				copy.setFactor(v, this.getFactor(v).copy());
		}


		return copy;
	}

	/**
	 * Add a new variable to the model. Added variables will be appended to the model and the index of the added
	 * variable will be returned. Adding a variable will always return a value that is greater than any other variable in
	 * the model.
	 *
	 * @param size - int the number of states in the variable
	 * @param exogenous - boolean indicating if the variable is exogenous.
	 * @return int - the label/index/id assigned to the variable
	 */
	public int addVariable(int size, boolean exogenous) {
		int vid = super.addVariable(size);
		if(exogenous)
			this.exogenousVars.add(vid);
		return vid;
	}

	/**
	 * Removes a variable from the model
	 * @param variable
	 */
	@Override
	public void removeVariable(int variable) {
		super.removeVariable(variable);
		if(exogenousVars.contains(variable))
			exogenousVars.remove(variable);

	}

	/**
	 * Array with IDs of the exogenous variables
	 * @return
	 */
	public int[] getExogenousVars() {
		return Ints.toArray(exogenousVars);
	}

	/**
	 * Allows to know if a variable is exogenous
	 * @param variable
	 * @return
	 */
	public boolean isExogenous(int variable){
		return exogenousVars.contains(variable);
	}

	/**
	 * Allows to know if a variable is endogenous
	 * @param variable
	 * @return
	 */
	public boolean isEndogenous(int variable) {
		return !isExogenous(variable);
	}

	/**
	 * Retruns an array with IDs of the endogenous variables
	 * @return
	 */
	public int[] getEndogenousVars() {
		Set<Integer> endogenousVars = new HashSet<Integer>();

		for(int v : this.getVariables())
			if(!this.exogenousVars.contains(v))
				endogenousVars.add(v)	;
		return Ints.toArray(endogenousVars);
	}


	/**
	 * Retruns an array with the IDs of parents that are exogenous variables
	 * @param v
	 * @return
	 */
	public int[] getExogenousParents(int v){
		return ArraysUtil.intersection(
				this.getExogenousVars(),
				this.getParents(v)
		);
	}

	/**
	 * Retruns an array with the IDs of parents that are endogenous variables
	 * @param v
	 * @return
	 */

	public int[] getEndegenousParents(int v){
		return ArraysUtil.intersection(
				this.getEndogenousVars(),
				this.getParents(v)
		);
	}


	/**
	 * Attach to each variable (endogenous or exogenous) a random factor.
	 * @param prob_decimals
	 */
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


	/**
	 * Gets the empirical probability of a endogenous variable by marginalizing out
	 * all its exogenous parents.
	 * @param var
	 * @return
	 */
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

	/**
	 * Returns a new SCM with the do operation done over a given variable.
	 * @param var - target variable.
	 * @param state - state to fix.
	 * @return
	 */
	@Override
	public StructuralCausalModel intervention(int var, int state){
		return (StructuralCausalModel)super.intervention(var, state);
	}


	/**
	 * Prints a summary of the SCM
	 */
	public void printSummary(){

		for(int x : this.getEndogenousVars()){

			System.out.println("\nEndogenous var "+x+" with "+ Arrays.toString(this.getSizes(x))+" states");
			System.out.println("Exogenous parents: "+Arrays.toString(this.getSizes(this.getExogenousParents(x)))+" states");

			BayesianFactor p = this.getProb(x).fixPrecission(5,x).reorderDomain(x);
			System.out.println(p+" = "+Arrays.toString(p.getData()));

			BayesianFactor f = this.getFactor(x).reorderDomain(this.getExogenousParents(x));




			double[][] fdata = ArraysUtil.reshape2d(f.getData(), f.getDomain().getCombinations()/f.getDomain().getCardinality(this.getExogenousParents(x)[0]));
			System.out.println(f+" = ");
			Stream.of(fdata).forEach(d -> System.out.println("\t"+Arrays.toString(d)));

//			System.out.println(f+" = "+Arrays.toString(f.getData()));

		}

	}


	/**
	 * Converts the current SCM into an equivalent credal network consistent
	 * with the empirical probabilities (of the endogenous variables).
	 * This is the simple case where each endogenous variable has a single
	 * and non-shared exogenous parent.
	 * @param empiricalProbs
	 * @return
	 */
	public SparseModel toVertexSimple(BayesianFactor... empiricalProbs){

		// Copy the structure of the this
		SparseModel cmodel = new SparseModel();
		cmodel.addVariables(this.getSizes(this.getVariables()));
		for (int v : cmodel.getVariables()){
			cmodel.addParents(v, this.getParents(v));
		}


		// Set the credal sets for the endogenous variables X
		for(int v: this.getEndogenousVars()) {
			VertexFactor kv = new BayesianToVertex().apply(this.getFactor(v), v);
			cmodel.setFactor(v, kv);
		}

		// Get the credal sets for the exogenous variables U
		for(int v: this.getExogenousVars()) {
			System.out.println("Calculating credal set for "+v);
			double [] vector = this.getFactor(this.getChildren(v)[0]).getData();


			double[][] coeff = ArraysUtil.transpose(ArraysUtil.reshape2d(
					this.getFactor(this.getChildren(v)[0]).getData(), this.getSizes(v)
			));

			int x = this.getChildren(v)[0];
			BayesianFactor pv = (BayesianFactor) Stream.of(empiricalProbs).filter(f ->
					ImmutableSet.copyOf(Ints.asList(f.getDomain().getVariables()))
							.equals(ImmutableSet.copyOf(
									Ints.asList(Ints.concat(new int[]{x}, this.getEndegenousParents(x))))))
					.toArray()[0];

			double[] vals = pv.getData();

			VertexFactor kv = new VertexFactor(cmodel.getDomain(v), coeff, vals);
			cmodel.setFactor(v, kv);
		}

		return cmodel;
	}



	/**
	 * Converts the current SCM into an equivalent credal network consistent
	 * with the empirical probabilities (of the endogenous variables).
	 * In this case exogenous parentes might have more than one endogenous child.
	 * @param empiricalProbs
	 * @return
	 */
	public SparseModel toVertexNonMarkov(BayesianFactor... empiricalProbs){

		// Copy the structure of the this
		SparseModel cmodel = new SparseModel();
		cmodel.addVariables(this.getSizes(this.getVariables()));
		for (int v : cmodel.getVariables()){
			cmodel.addParents(v, this.getParents(v));
		}

		// Set the credal sets for the endogenous variables X
		for(int v: this.getEndogenousVars()) {
			VertexFactor kv = new BayesianToVertex().apply(this.getFactor(v), v);
			cmodel.setFactor(v, kv);
		}

		// Get the credal sets for the exogenous variables U
		for(int v: this.getExogenousVars()) {

			System.out.println("Calculating credal set for "+v);
			double [] vector = this.getFactor(this.getChildren(v)[0]).getData();
			int[] children = this.getChildren(v);


			double[][] coeff = ArraysUtil.transpose(ArraysUtil.reshape2d(
					IntStream.of(children).mapToObj(i-> this.getFactor(i)).reduce((f1,f2) -> f1.combine(f2)).get()
							.getData(), this.getSizes(v)
			));

			BayesianFactor pv = (BayesianFactor) Stream.of(empiricalProbs).filter(f ->
					ImmutableSet.copyOf(Ints.asList(f.getDomain().getVariables()))
							.equals(ImmutableSet.copyOf(
									Ints.asList(children))))
					.toArray()[0];

			double[] vals = pv.getData();

			VertexFactor kv = new VertexFactor(cmodel.getDomain(v), coeff, vals);
			cmodel.setFactor(v, kv);
		}

		return cmodel;
	}

	/**
	 * Assuming that this SCM is a counterfactual model, this object
	 * associates the variables across the worlds.
	 * @return
	 */
	public CounterFactMapping getMap() {
		return map;
	}

	/**
	 * Assuming that this SCM is a counterfactual model, this object
	 * associates the variables across the worlds.
	 * @return
	 */
	public void setMap(CounterFactMapping map) {
		this.map = map;
	}


	/**
	 * Merge the current SCM with other equivalent ones to create a counterfactual model.
	 * @param models
	 * @return
	 */
	public StructuralCausalModel merge(StructuralCausalModel... models) {

		//check that the variables are the same
		for(StructuralCausalModel m : models){
			if (!Arrays.equals(this.getExogenousVars(), m.getExogenousVars()) ||
					!Arrays.equals(this.getEndogenousVars(), m.getEndogenousVars()))
				throw new IllegalArgumentException("Error: models cannot be merged");
		}

		//Indexes of variables should be consecutive
		if(this.getVariables().length != Ints.max(this.getVariables())+1){
			throw new IllegalArgumentException("Indexes of variables must be consecutive");
		}

		// get the new variables
		int[] merged_vars = IntStream.range(0, this.getVariables().length + models.length*this.getEndogenousVars().length).toArray();

		//counterfactual mapping
		CounterFactMapping map = new CounterFactMapping(merged_vars);

		// add variables of world 0 (reality)
		StructuralCausalModel merged = this.copy();
		IntStream.of(this.getEndogenousVars())
				.forEach(v->map.set(v,0,v));
		IntStream.of(this.getExogenousVars())
				.forEach(v->map.set(v,CounterFactMapping.ALL,v));


		int w = 1;
		for(StructuralCausalModel m: models){
			// Add all the endogenous variables
			for(int x_0: m.getEndogenousVars()) {
				int x_w = merged.addVariable(m.getSize(x_0));
				map.set(x_w,w,x_0);
			}
			// add the arcs
			for(int x_0: m.getEndogenousVars()) {
				int x_w = map.getEquivalentVars(w,x_0);
				for(int pa_0: m.getParents(x_0)) {
					int pa_w = pa_0;
					if(m.isEndogenous(pa_0))
						pa_w = map.getEquivalentVars(w, pa_0);;
					merged.addParent(x_w, pa_w);
				}
				// Set the factor with the new domain
				BayesianFactor f = m.getFactor(x_0);
				f = f.renameDomain(map.getEquivalentVars(w,f.getDomain().getVariables()));
				merged.setFactor(x_w, f);
			}
			w++;
		}

		// set the map
		merged.setMap(map);

		return merged;

	}

	/**
	 * String summarizing this SCM.
	 * @return
	 */
	public String toString(){

		StringBuilder str = new StringBuilder("");

		if(name != "") str.append(name+":");

		if(this.getMap()==null)
			str.append("\n"+this.getFactors());
		else{
			for(int w: this.getMap().getWorlds()){

				if (w == CounterFactMapping.ALL) {
					str.append("\nGlobal factors: ");
				} else {
					str.append("\n"+"World " + w + " factors: ");
				}

				for(int v: this.getMap().getVariablesIn(w)) {
					if (w == CounterFactMapping.ALL || this.getMap().getWorld(v) != CounterFactMapping.ALL)
						str.append("\n"+this.getFactor(v));
				}


			}
		}
		

		return str.toString()+"\n";
	}

	/**
	 * Returns the nane of the SCM.
	 * @return
	 */
	public String getName() {
		return name;
	}
}






