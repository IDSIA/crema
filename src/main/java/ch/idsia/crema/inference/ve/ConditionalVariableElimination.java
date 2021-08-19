package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.algebra.FactorAlgebra;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Ints;
import gnu.trove.map.TIntIntMap;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.08.2021 13:50
 */
public class ConditionalVariableElimination extends VariableElimination<BayesianFactor> {

	private int[] conditioning = new int[0];

	public ConditionalVariableElimination(int... conditioning) {
		super(new FactorAlgebra<>());
		setConditioning(conditioning);
	}

	public ConditionalVariableElimination(int[] sequence, int... conditioning) {
		super(new FactorAlgebra<>(), sequence);
		setConditioning(conditioning);
	}

	public void setConditioning(int... conditioning) {
		this.conditioning = conditioning;
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int query) {
		return query(model, evidence, new int[]{query});
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap observations, int... target) {
		conditioning = ArraysUtil.unique(Ints.concat(conditioning, observations.keys()));

		// Computes the join
		BayesianFactor numerator = run(Ints.concat(target, conditioning));

		BayesianFactor denomintor = numerator;
		for (int v : target) {
			if (ArraysUtil.contains(v, conditioning))
				throw new IllegalArgumentException("Variable " + v + " cannot be in target and conditioning set");
			denomintor = denomintor.marginalize(v);
		}

		// Conditional probability
		BayesianFactor cond = numerator.divide(denomintor);

		// Sets evidence
		for (int v : observations.keys())
			cond = cond.filter(v, observations.get(v));

		if (cond instanceof BayesianDefaultFactor)
			cond = ((BayesianDefaultFactor) cond).replaceNaN(0.0);

		return cond;
	}

}
