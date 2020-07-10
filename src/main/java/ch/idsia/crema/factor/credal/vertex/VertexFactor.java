package ch.idsia.crema.factor.credal.vertex;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.CredalFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import ch.idsia.crema.utility.SeparateIndexIterator;
import ch.idsia.crema.utility.hull.LPConvexHull;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.Relationship;

/**
 * A Separately specified Vertex based credal factor. TODO: Data is currenlty
 * not logged!
 * 
 * @author david
 *
 */
public class VertexFactor implements CredalFactor, SeparatelySpecified<VertexFactor>, Factor<VertexFactor> {
	private final static boolean log = false;

	private Strides separatedDomain;
	private Strides vertexDomain;

	private final double[][][] data;

	public static boolean CONVEX_HULL_MARG = false;

	public VertexFactor(Strides left, Strides right) {
		this.separatedDomain = right;
		this.vertexDomain = left;
		data = new double[right.getCombinations()][][];
	}

	public VertexFactor(Strides left, Strides right, double[][][] data) {
		this.separatedDomain = right;
		this.vertexDomain = left;
		this.data = data;
	}


	public VertexFactor(Strides left, double[][] coefficients, double[] values, Relationship... rel) {
		this.separatedDomain = Strides.empty();
		this.vertexDomain = left;
		data = new double[1][][];

		// check the coefficient sizes
		for(double[] c : coefficients){
			if (c.length != left.getCombinations())
				throw new IllegalArgumentException("ERROR: wrong constraint size: "+c.length+" instead of "+left.getCombinations());
		}

		// check the relationship vector length
		if(rel.length == 0) rel = new Relationship[] {Relationship.EQ};
		if(rel.length == 1) {
			Relationship[] rel_aux = new Relationship[coefficients.length];
			for(int i = 0; i< coefficients.length; i++)
				rel_aux[i] = rel[0];
			rel = rel_aux;
		}else if(rel.length != coefficients.length) {
			throw new IllegalArgumentException("ERROR: wrong relationship vector length: "+rel.length);
		}


		SeparateHalfspaceFactor k_const = new SeparateHalfspaceFactor(left, Strides.empty());
		for(int i=0; i< coefficients.length; i++){
				k_const.addConstraint(coefficients[i], rel[i], values[i]);
		}

		// normalization constraint
		double [] ones =  new double[left.getCombinations()];
		for(int i=0; i<ones.length; i++)
			ones[i] = 1.;
		k_const.addConstraint(ones, Relationship.EQ, 1.0);

		// non-negative constraints
		double [] zeros =  new double[left.getCombinations()];
		for(int i=0; i<zeros.length; i++)
			ones[i] = 0.;

		for(int i=0; i<left.getCombinations(); i++) {
			double[] c = zeros.clone();
			c[i] = 1.;
			k_const.addConstraint(c,Relationship.GEQ, 0);

		}

		HalfspaceToVertex conversor = new HalfspaceToVertex();
		double[][] vertices = conversor.apply(k_const,0).getData()[0];

		if(vertices == null || vertices.length == 0){
			throw new NoFeasibleSolutionException();
		}
		//add extreme points
		for(double[] v : vertices){
			this.addVertex(v);
		}
	}

	public VertexFactor(SeparateHalfspaceFactor constrainsFactor) {
		this.separatedDomain = constrainsFactor.getSeparatingDomain();
		this.vertexDomain = constrainsFactor.getDataDomain();
		data = new double[1][][];

		HalfspaceToVertex conversor = new HalfspaceToVertex();
		double[][] vertices = conversor.apply(constrainsFactor,0).getData()[0];

		if(vertices == null || vertices.length == 0){
			throw new NoFeasibleSolutionException();
		}
		//add extreme points
		for(double[] v : vertices){
			this.addVertex(v);
		}
	}




	@Override
	public VertexFactor copy() {
		double[][][] copy = ArraysUtil.deepClone(data);
		return new VertexFactor(vertexDomain, separatedDomain, copy);
	}

	@Override
	public Strides getDomain() {
		return separatedDomain.union(vertexDomain);
	}

	@Override
	public Strides getDataDomain() {
		return vertexDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return separatedDomain;
	}

	public double[][] getVertices(int... states) {
		int offset = separatedDomain.getOffset(states);
		return data[offset];
	}

	public double[][][] getData() {
		return data;
	}

	public void addVertex(double[] vertex, int... groupStates) {
		int offset = separatedDomain.getOffset(groupStates);
		double[][] gdata = data[offset];

		if (gdata != null) { // not first vertex
			int len = gdata.length;
			double[][] newdata = Arrays.copyOf(gdata, len + 1);
			newdata[len] = vertex;
			data[offset] = newdata;
		} else {
			data[offset] = new double[][] { vertex };
		}
	}

	public void removeVertex(int index, int... groupStates) {
		int offset = separatedDomain.getOffset(groupStates);
		double[][] gdata = data[offset];

		int len = gdata.length;
		double[][] newdata = new double[len - 1][];

		System.arraycopy(gdata, 0, newdata, 0, index);
		System.arraycopy(gdata, index + 1, newdata, index, len - index + 1);

		data[offset] = newdata;
	}

	/**
	 * Iterate over this object's domains extended to the specified domain
	 * 
	 * @param domain
	 * @return
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	private SeparateIndexIterator iterate(Strides domain) {
		IndexIterator data = vertexDomain.getSupersetIndexIterator(domain);
		IndexIterator group = separatedDomain.getSupersetIndexIterator(domain);
		return new SeparateIndexIterator(data, group);
	}

	/**
	 * Iterator over the two domains data and conditioning
	 * 
	 * @return
	 */
	public SeparateIndexIterator iterate() {
		IndexIterator data = new IndexIterator(vertexDomain);
		IndexIterator group = new IndexIterator(separatedDomain);
		return new SeparateIndexIterator(data, group);
	}

	
	@Override
	public VertexFactor filter(int variable, int state) {

		int var_offset = separatedDomain.indexOf(variable);
		if (var_offset < 0) {
			double[][][] newdata = new double[separatedDomain.getCombinations()][][];
			var_offset = vertexDomain.indexOf(variable);
			Strides newleft = vertexDomain.removeAt(var_offset);
			
			int offset = vertexDomain.getPartialOffset(new int[] { variable }, new int[] { state });
			
			for (int r = 0; r < separatedDomain.getCombinations(); ++r) {
				double[][] source = data[r];
				double[][] target = new double[source.length][newleft.getCombinations()];
				newdata[r] = target;
				
				IndexIterator iter = vertexDomain.getIterator(newleft);

				int tindex = 0;
				while(iter.hasNext()) {
					int i = iter.next();
					for (int vertex = 0; vertex < source.length; ++vertex) {
						target[vertex][tindex] = source[vertex][i + offset];
					}
					++tindex;
				}
			}
			
			return new VertexFactor(newleft, separatedDomain, newdata);
		} else {
			Strides newdomain = separatedDomain.removeAt(var_offset); // new
																		// Strides(separatedDomain,
																		// var_offset);
			IndexIterator iter = separatedDomain.getFiteredIndexIterator(variable, state);

			// should be replaceable with
			// int offset = separatedDomain.getPartialOffset(new int[] {
			// variable },
			// new int[] { state });
			// IndexIterator iter = separatedDomain.getIterator(separatedDomain,
			// variable);

			double[][][] newdata = new double[newdomain.getCombinations()][][];
			for (int index = 0; index < newdata.length; ++index) {
				newdata[index] = data[iter.next()];
			}
			return new VertexFactor(vertexDomain, newdomain, newdata);
		}
	}
	
	/**
	 * sum/marginalize some variables out of the credal set.
	 * 
	 * @param vars
	 * @return
	 */
	public VertexFactor marginalize(int... vars) {

		//System.out.println("marg "+Arrays.toString(vars)+" from "+toStringSimple());
		// only vars of the domain
		Strides sum_strides = getDataDomain().intersection(vars);
		Strides left = getDataDomain().remove(vars);
		double[][][] target_data = new double[data.length][][];
		for (int c = 0; c < data.length; ++c) {
			double[][] source = data[c];
			double[][] target = new double[source.length][left.getCombinations()];
			target_data[c] = target;

			IndexIterator src_remaining = getDataDomain().getIterator(left);
			IndexIterator src_summing = getDataDomain().getIterator(sum_strides, left.getVariables());
			for (int d = 0; d < left.getCombinations(); ++d) {
				int src = src_remaining.next();
				IndexIterator i = src_summing.clone();
				while (i.hasNext()) {
					int offset = i.next();
					for (int v = 0; v < source.length; ++v) {
						// FIXME this should be abstracted to support logsum
						target[v][d] += source[v][src + offset];
					}
				}
			}
		}

		VertexFactor f = new VertexFactor(left, getSeparatingDomain(), target_data);

		if(CONVEX_HULL_MARG)
			f.applyConvexHull(true);

		return f;



	}

	
	/**
	 * @param target
	 *            the new grouping/separation domain
	 * @return
	 */
	public VertexFactor reseparate(Strides target) {
		// requested current separation!
		if (Arrays.equals(target.getVariables(), separatedDomain.getVariables())) return this;
		
		Strides T = getSeparatingDomain().intersection(target);
		Strides Lt = getSeparatingDomain().remove(target);
		Strides Dl = getDataDomain().union(Lt);

		// target data
		double[][][] dest_data = new double[T.getCombinations()][][];

		// lets first iterate over the part of the grouping that stays the same
		IndexIterator src_right_offset_iter = getSeparatingDomain().getIterator(T);
		for (int dest_right = 0; dest_right < T.getCombinations(); ++dest_right) {
			int src_right_1 = src_right_offset_iter.next();

			// count vectors
			IndexIterator iter = getSeparatingDomain().getIterator(Lt);

			int[] vertex_counts = new int[Lt.getCombinations()];
			int index = 0;
			while (iter.hasNext()) {
				int idx = src_right_1 + iter.next();
				vertex_counts[index++] = data[idx].length;
			}

			int[] vvars = IntStream.range(0, index).toArray();
			Strides vstride = new Strides(vvars, vertex_counts);
			dest_data[dest_right] = new double[vstride.getCombinations()][Dl.getCombinations()];

			int vertex = 0;
			IndexIterator viter = vstride.getIterator(vstride);
			while (viter.hasNext()) {
				// get the list of selected vertices combination
				int[] locations = viter.getPositions();

				IndexIterator src_right_iter = getSeparatingDomain().getIterator(Dl, T.getVariables());
				IndexIterator src_left_iter = getDataDomain().getIterator(Dl);
				IndexIterator vindex = Lt.getIterator(Dl);

				for (int i = 0; i < Dl.getCombinations(); ++i) {
					int offset = vindex.next();

					int src_l = src_left_iter.next();
					int src_r_2 = src_right_iter.next();
					int src_r = src_r_2 + src_right_1;

					double v = data[src_r][locations[offset]][src_l];

					dest_data[dest_right][vertex][i] = v;
				}

				viter.next();
				++vertex;
			}
		}

		return new VertexFactor(Dl, T, dest_data);
	}



	@Override
	public VertexFactor combine(VertexFactor other) {

		//System.out.println("combine "+toStringSimple()+" with "+other.toStringSimple());

		if(!this.getDomain().isConsistentWith(other.getDomain())){
			throw new IllegalArgumentException("Factors domains are not consistent: "+this+", "+other);
		}

		// union
		Strides left = getDataDomain().union(other.getDataDomain());
		Strides runion = getSeparatingDomain().union(other.getSeparatingDomain());
		Strides right = runion.remove(left);

		VertexFactor reshaped1 = this.reseparate(right);
		VertexFactor reshaped2 = other.reseparate(right);

		double target_data[][][] = new double[right.getCombinations()][][];

		IndexIterator iter1 = reshaped1.getSeparatingDomain().getIterator(right);
		IndexIterator iter2 = reshaped2.getSeparatingDomain().getIterator(right);
		
		for (int r = 0; r < right.getCombinations(); ++r) {
			int idx1 = iter1.next();
			int idx2 = iter2.next();
			double[][] data1 = reshaped1.data[idx1];
			double[][] data2 = reshaped2.data[idx2];
			double[][] target = new double[data1.length * data2.length][left.getCombinations()];
			target_data[r] = target;

			for (int v1 = 0; v1 < data1.length; ++v1) {
				for (int v2 = 0; v2 < data2.length; ++v2) {
					double[] vertex = target[v1 + v2 * data1.length];
					double[] vertex1 = data1[v1];
					double[] vertex2 = data2[v2];

					IndexIterator i1 = reshaped1.getDataDomain().getIterator(left);
					IndexIterator i2 = reshaped2.getDataDomain().getIterator(left);
					for (int l = 0; l < left.getCombinations(); ++l) {
						int offset1 = i1.next();
						int offset2 = i2.next();

						vertex[l] = multiply(vertex1[offset1], vertex2[offset2]);
					}
				}
			}
		}

		return new VertexFactor(left, right, target_data);
	}

	/** as we might be in a log space, the multiplication might be a sum */
	private double multiply(double d, double e) {
		return log ? d + e : d * e;
	}

	@Override
	public String toString() {
		StringBuilder build = new StringBuilder();
		build.append("K(vars").append(Arrays.toString(getDataDomain().getVariables()));
		build.append("|").append(Arrays.toString(getSeparatingDomain().getVariables()));
		build.append(")\n");

		for (int i = 0; i < getDataDomain().getSize(); ++i) {
			build.append(getDataDomain().getVariables()[i]).append(": ");
			IndexIterator iter = getDataDomain().getIterator();
			while (iter.hasNext()) {
				build.append(iter.getPositions()[i]).append(",");
				iter.next();
			}
			build.append("\n");
		}

		IndexIterator condition = separatedDomain.getIterator();
		int[] c = condition.getPositions();
		int cp = 0;

		while (condition.hasNext()) {
			String head = "K(vars" + Arrays.toString(getDataDomain().getVariables()) + "|" + Arrays.toString(c) + ") ";
			String white = IntStream.range(0, head.length()).mapToObj(x -> " ").collect(Collectors.joining());
			for (int v = 0; v < data[cp].length; ++v) {
				build.append(head);
				build.append(Arrays.toString(data[cp][v]));
				build.append("\n");
				head = white;
			}
			condition.next();
			cp++;
		}
		return build.toString();
	}

	public String toStringSimple() {
		StringBuilder build = new StringBuilder();
		build.append("K(vars").append(Arrays.toString(getDataDomain().getVariables()));
		build.append("|").append(Arrays.toString(getSeparatingDomain().getVariables()));
		build.append(")");

		return build.toString();
	}


	public double[][][] getInternalData() {
		return data;
	}

	@Override
	public VertexFactor divide(VertexFactor other) {
		// 
		return null;
	}

	@Override
	public VertexFactor marginalize(int variable) {
		return this.marginalize(new int[]{variable});
	}
	
	public VertexFactor normalize() {
		double[][][] newdata = data.clone();
		for (int i = 0; i < data.length; ++i) {
			newdata[i] = data[i].clone();
			for (int v = 0; v < newdata[i].length; ++v) {
				double sum = Arrays.stream(data[i][v]).sum();
				newdata[i][v] = Arrays.stream(data[i][v]).map(x->x/sum).toArray();
			}
		}
		return new VertexFactor(vertexDomain, separatedDomain, newdata);
	}
	
	
	public Iterator<double[][]> getVertexSetIterator() {
		return Arrays.asList(data).iterator();
	}

	public double[][] getVerticesAt(int i) {
		return data[i];
	}


	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros).
	 * Thus, children variables are determined by the values of the parents
	 * @param left	Strides - children variables.
	 * @param right	Strides - parent variables
	 * @param assignments assignments of each combination of the parent
	 * @return
	 */
	public static VertexFactor deterministic(Strides left, Strides right, int... assignments){

		if (assignments.length != right.getCombinations())
			throw new IllegalArgumentException("ERROR: length of assignments should be equal to the number of combinations of the parents");

		if (Ints.min(assignments)<0 || Ints.max(assignments)>= left.getCombinations())
			throw new IllegalArgumentException("ERROR: assignments of deterministic function should be in the inteval [0,"+left.getCombinations()+")");


		VertexFactor f = new VertexFactor(left,right);

		for(int i=0; i< right.getCombinations(); i++){
			double[] values = new double[left.getCombinations()];
			values[assignments[i]] = 1.0;
			f.addVertex(values, i);
		}
		return f;
	}
	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 * @param left	Strides - children variables.
	 * @param assignment int - single value to assign
	 * @return
	 */

	public static VertexFactor deterministic(Strides left, int assignment){
		return VertexFactor.deterministic(left, Strides.empty(), assignment);
	}

	/**
	 * Static method that builds a deterministic factor (values can only be ones or zeros)
	 * without parent variables.
	 * @param var	int - id for the single children variable.
	 * @param assignment int - single value to assign
	 * @return
	 */

	public VertexFactor get_deterministic(int var, int assignment){
		return VertexFactor.deterministic(this.getDomain().intersection(var), assignment);
	}


	public VertexFactor getSingleVertexFactor(int... idx){

		int[] idx_arr;

		if(idx.length == 1) {
			idx_arr = IntStream.range(0,this.getSeparatingDomain().getCombinations())
					.map(i -> idx[0])
					.toArray();
		}else{
			idx_arr = Arrays.copyOf(idx, idx.length);
		}


		if(idx_arr.length != this.getSeparatingDomain().getCombinations()){
			throw new IllegalArgumentException("idx length should be equal to the number combinations of the parents.");
		}

		double[][][] data =
				IntStream.range(0, this.getSeparatingDomain().getCombinations())
						.mapToObj(i -> new double[][]{this.getVerticesAt(i)[idx_arr[i]]})
						.toArray(double[][][]::new);

		return new VertexFactor(this.getDataDomain(), this.getSeparatingDomain(), data);
	}


	public BayesianFactor sampleVertex(){

		int left_comb = this.getSeparatingDomain().getCombinations();

		int idx[] = IntStream.range(0,left_comb)
				.map(i-> RandomUtil.getRandom().nextInt(this.getVerticesAt(i).length))
				.toArray();

		double[] data =
				Doubles.concat(
						IntStream.range(0,left_comb)
								.mapToObj(i -> this.getVerticesAt(i)[RandomUtil.getRandom().nextInt(this.getVerticesAt(i).length)])
								.toArray(double[][]::new)
				);


		Strides newDomain = this.getDataDomain().concat(this.getSeparatingDomain());
		return new BayesianFactor(newDomain, data);
	}


	public void applyConvexHull(boolean simplex){
		for(int i=0; i<this.getSeparatingDomain().getCombinations(); i++) {
			data[i] = LPConvexHull.compute(data[i], true);
		}
	}

	public VertexFactor convexHull(boolean simplex){
		VertexFactor f = this.copy();
		f.applyConvexHull(simplex);
		return f;
	}

	/**
	 * Replaces the IDs of the variables in the domain
	 * @param new_vars
	 * @return
	 */

	@Override
	public VertexFactor renameDomain(int... new_vars){

		int[] leftIdx = IntStream.range(0, this.vertexDomain.getVariables().length).toArray();
		int[] rightIdx = IntStream.range(this.vertexDomain.getVariables().length, new_vars.length).toArray();


		Strides leftStrides = new Strides(
				ArraysUtil.slice(new_vars, leftIdx),
				ArraysUtil.slice(getDomain().getSizes(), leftIdx)
		);

		Strides rightStrides = new Strides(
				ArraysUtil.slice(new_vars, rightIdx),
				ArraysUtil.slice(getDomain().getSizes(), rightIdx)
		);

		VertexFactor out = new VertexFactor(leftStrides, rightStrides, this.getData());
		return out;
	}



}
