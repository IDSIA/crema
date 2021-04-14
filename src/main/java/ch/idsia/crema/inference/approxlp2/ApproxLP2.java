package ch.idsia.crema.inference.approxlp2;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.search.impl.GreedyWithRandomRestart;
import gnu.trove.map.TIntIntMap;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.HashMap;
import java.util.Map;

public class ApproxLP2<F extends GenericFactor> implements Inference<GraphicalModel<F>, IntervalFactor> {

	private Map<String, Object> init = null;
	private boolean preprocess = true;

	public ApproxLP2() {
	}

	public ApproxLP2(boolean preprocess) {
		this.preprocess = preprocess;
	}

	public ApproxLP2(Map<String, ?> params) {
		initialize(params);
	}

	public ApproxLP2(Map<String, ?> params, boolean preprocess) {
		this.preprocess = preprocess;
		initialize(params);
	}

	/**
	 * @param params configuration map for the inference engine
	 */
	public void initialize(Map<String, ?> params) {
		if (params == null)
			this.init = new HashMap<>();
		else
			this.init = new HashMap<>(params);
	}

	public void setPreprocess(boolean preprocess) {
		this.preprocess = preprocess;
	}

	/**
	 * @deprecated use method {@link #query(GraphicalModel, TIntIntMap, int)}
	 */
	@Deprecated
	public IntervalFactor query(GraphicalModel<F> originalModel, int query, TIntIntMap evidence) {
		return query(originalModel, evidence, query);
	}

	/**
	 * No need to remove barren variables!
	 * <p>
	 * Preconditions: model reduction (root node observations, single
	 * node evidence. Factors must be of type ExtensiveLinearFactors,
	 * BayesianFactor or SeparateLinearFactor
	 * <p>
	 * XXX must support multiple evidence here and in the variable elimination
	 *
	 * @param originalModel the data model
	 * @param evidence      the variable that is to be considered the summarization of the
	 *                      evidence (-1 if no evidence)
	 * @param query         the variable whose intervals we are interested in
	 * @return the result of the inference
	 */
	@Override
	public IntervalFactor query(GraphicalModel<F> originalModel, TIntIntMap evidence, int query) {
		GraphicalModel<F> model = originalModel;
		if (preprocess) {
			RemoveBarren<F> remove = new RemoveBarren<>();
			model = remove.execute(originalModel, evidence, query);
		}

		int states = model.getSize(query);

		double[] lowers = new double[states];
		double[] uppers = new double[states];

		for (int state = 0; state < states; ++state) {
			Manager lower;
			Manager upper;

			if (evidence.isEmpty()) {
				// without evidence we are looking for a marginal
				lower = new Marginal(model, GoalType.MINIMIZE, query, state);
				upper = new Marginal(model, GoalType.MAXIMIZE, query, state);
			} else {
				lower = new Posterior(model, GoalType.MINIMIZE, query, state, evidence);
				upper = new Posterior(model, GoalType.MAXIMIZE, query, state, evidence);
			}

			lowers[state] = runSearcher(model, lower);
			uppers[state] = runSearcher(model, upper);

		}

		IntervalFactor result = new IntervalFactor(
				model.getDomain(query), model.getDomain(), new double[][]{lowers}, new double[][]{uppers}
		);
		result.updateReachability();

		return result;
	}

	private double runSearcher(GraphicalModel<F> model, Manager objective) {
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

		try {
			return searcher.run();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

}
