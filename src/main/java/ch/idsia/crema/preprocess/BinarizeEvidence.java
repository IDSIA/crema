package ch.idsia.crema.preprocess;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.graphical.MixedModel;
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
 * Currently we are using {@link BayesianFactor} only.
 *
 * @author david
 */
// TODO we should consider the possibility to specify a method/lambda, or something else, to customize the precise factor creation.
public class BinarizeEvidence<F extends GenericFactor> implements ConverterEvidence<F, GenericFactor, GraphicalModel<F>, MixedModel> {

	private int evidenceNode;
	private int size = 2;
	private boolean log = false;

	public BinarizeEvidence() {
	}

	public BinarizeEvidence(boolean log) {
		setLog(log);
	}

	public BinarizeEvidence(int size) {
		setSize(size);
	}

	public BinarizeEvidence(int size, boolean log) {
		setSize(size);
		setLog(log);
	}

	/**
	 * @param size the size of the binarized evidence
	 */
	// TODO: what is this? It is just the size of evidence?
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @param log true if the evidence node should use the log probabilities
	 */
	public void setLog(boolean log) {
		this.log = log;
	}

	public int getEvidenceNode() {
		return evidenceNode;
	}

	@Deprecated
	public GraphicalModel<GenericFactor> execute(GraphicalModel<F> model, TIntIntMap evidence, int size, boolean log) {
		setSize(size);
		setLog(log);
		return execute(model, evidence);
	}

	/**
	 * @deprecated use method{@link #execute(GraphicalModel, TIntIntMap)}
	 */
	@Deprecated
	public int executeInplace(GraphicalModel<F> model, TIntIntMap evidence, int size, boolean log) {
		setSize(size);
		setLog(log);
		execute(model, evidence);
		return evidenceNode;
	}

	/**
	 * Execute the binarization and return a new Factor
	 *
	 * @param original the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @return a mixed model where a new node of type {@link BayesianFactor} is added to collect all the evidence
	 */
	@Override
	public MixedModel execute(GraphicalModel<F> original, TIntIntMap evidence) {
		MixedModel model = new MixedModel((DAGModel<GenericFactor>) original.copy());
		int[] keys = evidence.keys();

		// TODO: XXX do we need to sort the keys????

		int new_var = -1;

		ArrayList<Instance> parents = new ArrayList<>();
		for (int var : keys) {
			int var_size = model.getSize(var);
			int var_obs = evidence.get(var);

			parents.add(new Instance(var, var_size, var_obs));
			if (parents.size() == size) {
				new_var = create(model, parents);
				parents.clear();
				parents.add(new Instance(new_var, 2, 1));
			}
		}

		// when no dummy has been created we must create it
		// when we did create dummies, the parents array will contain the last
		// created dummy and all vars not yet dummied
		if (new_var == -1 || parents.size() > 1) {
			new_var = create(model, parents);
		}
		evidenceNode = new_var;

		return model;
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

	private int create(MixedModel model, List<Instance> parents) {
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