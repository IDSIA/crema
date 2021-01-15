package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.SingleInference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.search.impl.GreedyWithRandomRestart;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.HashMap;
import java.util.Map;

public class Inference<F extends GenericFactor> implements SingleInference<F, IntervalFactor> {

	public static double EPS = 0.0;

	public IntervalFactor query(GraphicalModel<? extends GenericFactor> model, int query) throws InterruptedException {
		return query(model, query, -1);
	}

	/**
	 * Preconditions: model reduction (barren and root node observations, single
	 * node evidence. Factors must be of type ExtensiveLinearFactors,
	 * BayesianFactor or SeparateLinearFactor
	 * <p>
	 * XXX must support multiple evidence here and in the variable elimination
	 *
	 * @param model    the data model
	 * @param query    the variable whose intervals we are interested in
	 * @param evidence the variable that is to be considered the summarization of the
	 *                 evidence (-1 if no evidence)
	 * @return
	 * @throws InterruptedException
	 */
	public IntervalFactor query(GraphicalModel<? extends GenericFactor> model, int query, int evidence) throws InterruptedException {
		int states = model.getSize(query);

		double[] lowers = new double[states];
		double[] uppers = new double[states];

		for (int state = 0; state < states; ++state) {
			Manager lower;
			Manager upper;

			if (evidence == -1) {
				// without evidence we are looking for a marginal
				EPS = 0.0;
				lower = new Marginal(model, GoalType.MINIMIZE, query, state);
				upper = new Marginal(model, GoalType.MAXIMIZE, query, state);
			} else {
				EPS = 0.000000001;
				lower = new Posterior(model, GoalType.MINIMIZE, query, state, evidence);
				upper = new Posterior(model, GoalType.MAXIMIZE, query, state, evidence);
			}

			lowers[state] = runSearcher(model, lower);
			uppers[state] = runSearcher(model, upper);

		}

		IntervalFactor result = new IntervalFactor(model.getDomain(query), model.getDomain(), new double[][]{lowers},
				new double[][]{uppers});
		result.updateReachability();

		return result;
	}

	private double runSearcher(GraphicalModel<? extends GenericFactor> model, Manager objective)
			throws InterruptedException {

		Neighbourhood neighbourhood = new Neighbourhood(model);

		GreedyWithRandomRestart<Move, Solution> searcher = new GreedyWithRandomRestart<>();
		searcher.setNeighbourhoodFunction(neighbourhood);
		searcher.setObjectiveFunction(objective);

		HashMap<String, Object> opt = new HashMap<>();
		opt.put(GreedyWithRandomRestart.MAX_RESTARTS, "10");
		opt.put(GreedyWithRandomRestart.MAX_PLATEAU, "3");

		if (init != null)
			opt.putAll(init);
		searcher.initialize(neighbourhood.random(), opt);

		return searcher.run();
	}

	private Map<String, Object> init = null;

	public void initialize(Map<String, ?> params) {
		if (params == null)
			this.init = new HashMap<>();
		else
			this.init = new HashMap<>(params);
	}

	@Override
	public IntervalFactor apply(GraphicalModel<F> model, int query, TIntIntMap observations) throws InterruptedException {
		if (observations.isEmpty()) {
			return query(model, query);
		} else {
			return null;
		}
	}

}
