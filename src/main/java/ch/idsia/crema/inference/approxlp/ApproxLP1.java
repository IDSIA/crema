package ch.idsia.crema.inference.approxlp;

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


/**
 * Perform inference using ApproxLP
 */
public class ApproxLP1<F extends GenericFactor> implements Inference<GraphicalModel<F>, IntervalFactor> {

	public static double EPS = 0.0;

	private Map<String, Object> init = null;

	protected int evidenceNode = -1;

	public void initialize(Map<String, ?> params) {
		if (params == null)
			this.init = new HashMap<>();
		else
			this.init = new HashMap<>(params);
	}

	public void setEvidenceNode(int evidenceNode) {
		this.evidenceNode = evidenceNode;
	}

	/**
	 * Preconditions:
	 * <ul>
	 * <li>single evidence node
	 * <li>Factors must be one of <ul>
	 * 		<li>ExtensiveLinearFactors,
	 * 		<li>BayesianFactor or
	 * 		<li> SeparateLinearFactor
	 * </ul></ul>
	 * <p>
	 *     Use the method {@link #setEvidenceNode(int)} to set the variable that is to be considered the summarization
	 *     of the evidence (-1 if no evidence).
	 * <p>
	 *
	 * @param originalModel the data model
	 * @param evidence      the observed variable as a map of variable-states
	 * @param query         the variable whose intervals we are interested in
	 * @return
	 */
	// TODO must support multiple evidence here and in the variable elimination (see {@link ch.idsia.crema.inference.approxlp2.ApproxLP2})
	// TODO: should we binarize the evidence?
	@Override
	public IntervalFactor query(GraphicalModel<F> originalModel, TIntIntMap evidence, int query) {
		final RemoveBarren<F> remove = new RemoveBarren<>();
		final GraphicalModel<F> model = remove.execute(originalModel, evidence, query);

		int states = model.getSize(query);

		double[] lowers = new double[states];
		double[] uppers = new double[states];

		for (int state = 0; state < states; ++state) {
			Manager lower;
			Manager upper;

			if (evidenceNode == -1) {
				// without evidence we are looking for a marginal
				EPS = 0.0;
				lower = new Marginal(model, GoalType.MINIMIZE, query, state);
				upper = new Marginal(model, GoalType.MAXIMIZE, query, state);
			} else {
				EPS = 0.000000001;
				lower = new Posterior(model, GoalType.MINIMIZE, query, state, evidenceNode);
				upper = new Posterior(model, GoalType.MAXIMIZE, query, state, evidenceNode);
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
		try {
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
		} catch (InterruptedException e) {
			// TODO: maybe return NaN?
			throw new IllegalStateException(e);
		}
	}

	@Deprecated
	public IntervalFactor apply(GraphicalModel<F> model, int query, TIntIntMap observations) {
		if (observations.isEmpty()) {
			return query(model, query);
		} else {
			return null;
		}
	}

}
