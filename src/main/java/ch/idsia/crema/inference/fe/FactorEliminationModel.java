package ch.idsia.crema.inference.fe;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

import static ch.idsia.crema.inference.fe.FactorEliminationUtils.project;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 09:37
 */
// TODO: this is the main FactorElimination since it works with a GraphicalModel
public class FactorEliminationModel implements Inference<DAGModel<BayesianFactor>, BayesianFactor> {

	/**
	 * Algorithm 9 from "Modeling and Reasoning with BN", Dawiche, p.153
	 *
	 * @param model model to work with
	 * @param query variable to query
	 * @return the prior marginal on the query variable
	 */
	@Override
	public BayesianFactor query(DAGModel<BayesianFactor> model, int query) {
		// CPTs of network N
		List<BayesianFactor> S = new ArrayList<>(model.getFactors());

		// a factor in S that contains variable Q
		BayesianFactor fr = findAFactor(S, query);

		// while S has more than one factor
		while (S.size() > 1) {
			BayesianFactor fi = findFi(S, fr);
			if (fi == null)
				continue;

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
			if (!f.equals(Fr)) {
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

	@Override
	public BayesianFactor query(DAGModel<BayesianFactor> model, TIntIntMap evidence, int query) {
		throw new IllegalArgumentException(this.getClass().getName() + " doesn't support evidence");
	}
}
