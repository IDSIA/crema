package ch.idsia.crema.preprocess.creators;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.MixedModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.03.2021 18:44
 */
public class CreateFactorBayesian implements CreateFactor {

	private final boolean log;

	public CreateFactorBayesian() {
		this(false);
	}

	/**
	 * @param log true if the evidence node should use the log probabilities
	 */
	public CreateFactorBayesian(boolean log) {
		this.log = log;
	}

	@Override
	public int create(MixedModel model, List<Instance> parents) {
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
