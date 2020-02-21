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


	/**
	 * Create the directed model using the specified network implementation.
	 */
	public StructuralCausalModel() {
		super(new SparseDirectedAcyclicGraph());
	}

	private Set<Integer> exogenousVars = new HashSet<Integer>();
	
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


	public static StructuralCausalModel getCausalStructFromDAG(SparseDirectedAcyclicGraph dag, int[] endoVarSizes, int... exoVarSizes){

		if(endoVarSizes.length != dag.getVariables().length)
			throw new IllegalArgumentException("endoVarSizes vector should as long as the number of vertices in the dag");


//		if(exoVarSizes.length> 1 && exoVarSizes.length != dag.getVariables().length)
//			throw new IllegalArgumentException("exoVarSizes vector should be a vector of lenght 1 or as long as the number of vertices in the dag");

		Strides dagDomain = new Strides(dag.getVariables(), endoVarSizes);

		if(exoVarSizes.length==0){
			exoVarSizes =  IntStream.of(dag.getVariables())
					.map(v -> dagDomain.intersection(ArrayUtils.add(dag.getParents(v), v)).getCombinations()+1)
					.toArray();
		}else if(exoVarSizes.length==1){
			int s = exoVarSizes[0];
			exoVarSizes = IntStream.range(0,dag.getVariables().length).map(i-> s).toArray();
		}


		StructuralCausalModel smodel = new StructuralCausalModel();
		smodel.addVariables(dagDomain.getSizes());

		for (int i = 0; i<dag.getVariables().length; i++){
			int v = dag.getVariables()[i];
			smodel.addParents(v, dag.getParents(v));
			if(exoVarSizes.length==1)
				smodel.addParent(v, smodel.addVariable(exoVarSizes[0], true));
			else
				smodel.addParent(v, smodel.addVariable(exoVarSizes[i], true));
		}

		return smodel;
	}



	public SparseModel toVertexSimple(BayesianFactor... factors){

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
			BayesianFactor pv = (BayesianFactor) Stream.of(factors).filter(f ->
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



	public SparseModel toVertexNonMarkov(BayesianFactor... factors){

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

			BayesianFactor pv = (BayesianFactor) Stream.of(factors).filter(f ->
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

	
	

	public int[] getExogenousParents(int v){
		return ArraysUtil.intersection(
				this.getExogenousVars(),
				this.getParents(v)
		);
	}

	public int[] getEndegenousParents(int v){
		return ArraysUtil.intersection(
				this.getEndogenousVars(),
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

	@Override
	public StructuralCausalModel intervention(int var, int state){
		return (StructuralCausalModel)super.intervention(var, state);
	}


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
	 * 
	 * @return
	 */
	public CounterFactMapping getMap() {
		return map;
	}

	/**
	 * 
	 * @param map
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

	
	public String toString(){

		StringBuilder str = new StringBuilder("");

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



}






