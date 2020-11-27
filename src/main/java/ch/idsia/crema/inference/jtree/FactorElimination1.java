package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.SingleInference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 09:37
 */
public class FactorElimination1 implements SingleInference<BayesianFactor, BayesianFactor> {
	private GraphicalModel<BayesianFactor> model;

	public void setModel(GraphicalModel<BayesianFactor> model) {
		this.model = model.copy();
	}

	/**
	 * Algorithm 9 from "Modeling and Reasoning with BN", Dawiche, p.153
	 *
	 * @param query variable to query
	 * @return the prior marginal on the query variable
	 */
	public BayesianFactor FE1(int query) {

		// CPTs of network N
		List<BayesianFactor> S = new ArrayList<>(model.getFactors());

		// a factor in S that contains variable Q
		BayesianFactor fr = findAFactor(S, query);

		// while S has more than one factor
		while (S.size() > 1) {
			BayesianFactor fi = findFi(S, fr);
			TIntSet V = variablesInFiNotInS(fi, S);
			BayesianFactor fj = S.remove(S.size() - 1);

			for (int v : V.toArray()) fi = fi.marginalize(v);

			BayesianFactor combine = fj.combine(fi);
			S.add(combine);
		}

		fr = S.get(0);

		return project(fr, query);
	}

	/**
	 * Finds a {@link BayesianFactor} in S that contains the query variable Q.
	 *
	 * @param S list of factors in the model
	 * @param Q the query variable
	 * @return a factor that contains the query variable
	 */
	private BayesianFactor findAFactor(List<BayesianFactor> S, int Q) {
		BayesianFactor fr = null;
		for (BayesianFactor s : S) {
			if (s.getDomain().contains(Q)) {
				fr = s;
				break;
			}
		}

		return fr;
	}

	/**
	 * Finds a {@link BayesianFactor} Fi such that it is not equals to the Fr factor with the query
	 *
	 * @param S  the list of factors
	 * @param Fr the factor to avoid
	 * @return a valid factor
	 */
	private BayesianFactor findFi(List<BayesianFactor> S, BayesianFactor Fr) {
		// loops from the end
		for (int i = S.size() - 1; i >= 0; i--) {
			BayesianFactor f = S.get(i);
			if (!S.equals(Fr)) {
				S.remove(i);
				return f;
			}
		}
		return null;
	}

	/**
	 * List all the variables that are in the {@link BayesianFactor} Fi and not in the factor list S.
	 *
	 * @param Fi the current factor
	 * @param S  the list of factors
	 * @return a set of all the variables in Fi and not in S.
	 */
	private TIntSet variablesInFiNotInS(BayesianFactor Fi, List<BayesianFactor> S) {
		TIntSet fiVariables = new TIntHashSet(Fi.getDomain().getVariables());
		TIntSet sVariables = new TIntHashSet();

		for (BayesianFactor f : S) {
			sVariables.addAll(f.getDomain().getVariables());
		}

		fiVariables.removeAll(sVariables);

		return fiVariables;
	}

	/**
	 * Given a {@link BayesianFactor} Fr, marginalize all the variables of this factor that are not equals to the
	 * query variable Q.
	 *
	 * @param Fr the target factor
	 * @param Q  the query variable
	 * @return a factor over the query variable
	 */
	private BayesianFactor project(BayesianFactor Fr, int Q) {

		TIntSet variables = new TIntHashSet(Fr.getDomain().getVariables());
		variables.remove(Q);

		for (int v : variables.toArray())
			Fr = Fr.marginalize(v);

		return Fr;
	}

	@Override
	public BayesianFactor apply(GraphicalModel<BayesianFactor> model, int query, TIntIntMap observations) {
		throw new IllegalArgumentException("Factor Elimination 1 doesn't support observations!");
	}

	@Override
	public BayesianFactor apply(GraphicalModel<BayesianFactor> model, int query) {
		setModel(model);
		return FE1(query);
	}
}
