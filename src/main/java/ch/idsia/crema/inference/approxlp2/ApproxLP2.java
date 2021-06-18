package ch.idsia.crema.inference.approxlp2;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalDefaultFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
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

	private boolean noSearch = false;
	private int maxGenerations = 10;

	public ApproxLP2() {
	}

	/**
	 * By default, the preprocessing methods is just {@link RemoveBarren<F>}.
	 *
	 * @param preprocess apply preprocessing methods to the model before inference (default: true)
	 */
	public ApproxLP2(boolean preprocess) {
		this.preprocess = preprocess;
	}

	/**
	 * Instead of use the optimized search, generate random networks.
	 *
	 * @param maxGenerations max generation to perform (default: 10)
	 */
	public ApproxLP2(int maxGenerations) {
		this.noSearch = true;
		this.maxGenerations = maxGenerations;
	}

	public ApproxLP2(int maxGenerations, boolean preprocess) {
		this(maxGenerations);
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
	 * No need to remove barren variables!
	 * <p>
	 * Preconditions: model reduction (root node observations, single
	 * node evidence. Factors must be of type ExtensiveLinearFactors,
	 * BayesianFactor or SeparateLinearFactor
	 * <p>
	 *
	 * @param originalModel the data model
	 * @param evidence      the variable that is to be considered the summarization of the
	 *                      evidence (-1 if no evidence)
	 * @param query         the variable whose intervals we are interested in
	 * @return the result of the inference
	 */
	// TODO must support multiple evidence here and in the variable elimination
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

		return new IntervalDefaultFactor(
				model.getDomain(query), model.getDomain(), new double[][]{lowers}, new double[][]{uppers}
		)
				.updateReachability();
	}

	private double runSearcher(GraphicalModel<F> model, Manager objective) {
		Neighbourhood neighbourhood = new Neighbourhood(model);

		if (noSearch) {
			// TODO: this should be another AbstractSearcher (SimpleRandomSearch?)
			Solution optimal = neighbourhood.random();
			double opt_score = objective.eval(optimal);

			for (int i = 0; i < maxGenerations; i++) {
				Solution solution = neighbourhood.random();
				double score = objective.eval(optimal);

				if (objective.getGoal() == GoalType.MAXIMIZE) {
					if (score > opt_score) {
						opt_score = score;
						optimal = solution;
					}
				} else {
					if (score < opt_score) {
						opt_score = score;
						optimal = solution;
					}
				}
			}

			return opt_score;
		}

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
