package ch.idsia.crema.preprocess;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Multiple evidences can be reduced to a single binary variable by adding
 * deterministic nodes as children of the observed nodes.
 *
 * <p>
 * This is usefull for algorithms that do not filter the observed evidences out
 * of the factors until the end.
 * </p>
 * <p>
 * TODO we should consider the possibility to specify a method/lambda, or something else, to customize the precise factor creation.
 * Currently we are using {@link BayesianFactor} only.
 *
 * @author david
 */
public class BinarizeEvidence {

	private int leafDummy;

	public int getLeafDummy() {
		return leafDummy;
	}

	/**
	 * Execute the binarization and return a new Factor
	 *
	 * @param model
	 * @param evidence
	 * @param size
	 * @param log
	 * @return
	 */
	public GraphicalModel<GenericFactor> execute(GraphicalModel<? super GenericFactor> model, TIntIntMap evidence, int size, boolean log) {
		@SuppressWarnings("unchecked")
		GraphicalModel<GenericFactor> copy = (GraphicalModel<GenericFactor>) model.copy();
		leafDummy = executeInplace(copy, evidence, size, log);
		return copy;
	}

	public int executeInplace(GraphicalModel<GenericFactor> model, TIntIntMap evidence, int size, boolean log) {
		int[] keys = evidence.keys();

		// TODO: XXX do we need to sort the keys????

		int new_var = -1;

		ArrayList<Instance> parents = new ArrayList<>();
		for (int var : keys) {
			int var_size = model.getSize(var);
			int var_obs = evidence.get(var);

			parents.add(new Instance(var, var_size, var_obs));
			if (parents.size() == size) {
				new_var = create(model, parents, log);
				parents.clear();
				parents.add(new Instance(new_var, 2, 1));
			}
		}

		// when no dummy has been created we must create it
		// when we did create dummies, the parents array will contain the last
		// created
		// dummy and all vars not yet dummied
		if (new_var == -1 || parents.size() > 1) {
			new_var = create(model, parents, log);
		}
		leafDummy = new_var;
		return new_var;
	}

	private static class Instance implements Comparable<Instance> {
		public int variable;
		public int size;
		public int observed;

		public Instance(int variable, int size, int observed) {
			this.variable = variable;
			this.size = size;
			this.observed = observed;
		}

		@Override
		public int compareTo(Instance o) {
			return variable - o.variable;
		}
	}

	private int create(GraphicalModel<? super GenericFactor> model, List<Instance> parents, boolean log) {
		int conf = 1;
		int offset = 0;

		// parents must be sorted
		Collections.sort(parents);

		int[] variables = new int[parents.size() + 1];
		int[] sizes = new int[parents.size() + 1];
		int[] strides = new int[parents.size() + 2];
		int index = 0;

		for (Instance parent : parents) {
			variables[index] = parent.variable;
			sizes[index] = parent.size;
			strides[index] = conf;

			offset += conf * parent.observed;
			conf *= parent.size; // FIXME: this cause conf to be 0 if the parent.size is zero!
			++index;
		}

		// var is added to the end and ID will be the the biggest var number
		int id = model.addVariable(2);

		for (Instance parent : parents) {
			model.addParent(id, parent.variable);
		}

		variables[index] = id;
		sizes[index] = 2;
		strides[index] = conf;
		strides[index + 1] = conf * 2;

		Strides domain = new Strides(variables, sizes, strides);

		// since the added variable is always the last in the domain we can
		// create the factor's data array as the concatenation of two blocks:
		// the first is a set of 0 with exception of offset that is set to 1
		// the second is the inverse.
		double[] data = new double[conf * 2];
		Arrays.fill(data, 0, conf, 1.0);

		data[offset] = 0;
		data[conf + offset] = 1;

		// create the factor 
		BayesianFactor factor = new BayesianFactor(domain, data, log);
		model.setFactor(id, factor);

		return id;
	}
}