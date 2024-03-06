package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.algebra.FactorAlgebra;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import com.google.common.primitives.Ints;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    19.08.2021 13:50
 */
public class ConditionalVariableElimination extends VariableElimination<BayesianFactor> {

	private IntSet conditioning = new IntOpenHashSet();

	public ConditionalVariableElimination(int[] sequence, int... conditioning) {
		super(sequence);
		setConditioning(conditioning);
	}

	public void setConditioning(int... conditioning) {
		this.conditioning.addAll(new IntArrayList(conditioning));
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, Int2IntMap evidence, int query) {
		return query(model, evidence, new int[]{query});
	}

	@Override
	public BayesianFactor query(GraphicalModel<BayesianFactor> model, Int2IntMap observations, int... target) {
		conditioning.addAll(observations.keySet());

		IntSet t = new IntOpenHashSet(target);
		t.addAll(conditioning);
		
		// Computes the join
		BayesianFactor numerator = run(t.toIntArray());

		BayesianFactor denomintor = numerator;
		for (int v : target) {
			if (conditioning.contains(v))
				throw new IllegalArgumentException("Variable " + v + " cannot be in target and conditioning set");
			denomintor = denomintor.marginalize(v);
		}

		// Conditional probability
		BayesianFactor cond = numerator.divide(denomintor);

		// Sets evidence
		for (int v : observations.keySet())
			cond = cond.filter(v, observations.get(v));

		if (cond instanceof BayesianDefaultFactor)
			cond = ((BayesianDefaultFactor) cond).replaceNaN(0.0);

		return cond;
	}

	public BayesianFactor query(GraphicalModel<BayesianFactor> model, int... target) {
		return this.query(model, new Int2IntOpenHashMap(), target);
	}

}
