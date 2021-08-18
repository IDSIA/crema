package ch.idsia.crema.inference.approxlp1;

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


/**
 * Perform inference using ApproxLP
 */
public class ApproxLP1<F extends GenericFactor> implements Inference<GraphicalModel<F>, IntervalFactor> {

	public static double EPS = 1e-9;

	private Map<String, Object> init = null;

	private boolean preprocess = true;

	protected int evidenceNode = -1;

	public ApproxLP1() {
	}

	public ApproxLP1(boolean preprocess) {
		this.preprocess = preprocess;
	}

	public ApproxLP1(int evidenceNode) {
		setEvidenceNode(evidenceNode);
	}

	public ApproxLP1(Map<String, ?> params) {
		initialize(params);
	}

	public ApproxLP1(Map<String, ?> params, boolean preprocess) {
		this.preprocess = preprocess;
		initialize(params);
	}

	public ApproxLP1(Map<String, ?> params, int evidenceNode) {
		initialize(params);
		setEvidenceNode(evidenceNode);
	}

	public ApproxLP1(Map<String, ?> params, int evidenceNode, boolean preprocess) {
		this.preprocess = preprocess;
		initialize(params);
		setEvidenceNode(evidenceNode);
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
	 * @param evidenceNode the node found with a {@link ch.idsia.crema.preprocess.BinarizeEvidence} pre-processing
	 */
	public void setEvidenceNode(int evidenceNode) {
		this.evidenceNode = evidenceNode;
	}

	/**
	 * Perform a {@link RemoveBarren} on the input model given the query and the evidence before calling the method
	 * {@link #query(GraphicalModel, int)};
	 *
	 * @param originalModel the inference model
	 * @param evidence      the observed variable as a map of variable-states
	 * @param query         the variable that will be queried
	 * @return
	 */
	@Override
	public IntervalFactor query(GraphicalModel<F> originalModel, TIntIntMap evidence, int query) {
		GraphicalModel<F> model = originalModel;
		if (preprocess) {
			final RemoveBarren<F> remove = new RemoveBarren<>();
			model = remove.execute(originalModel, evidence, query);
		}
		return query(model, query);
	}

	/**
	 * Preconditions:
	 * <ul>
	 * <li>single evidence node
	 * <li>Factors must be one of
	 * <ul>
	 * 		<li>ExtensiveLinearFactors,
	 * 		<li>BayesianFactor or
	 * 		<li>SeparateLinearFactor
	 * </ul>
	 * </ul>
	 * <p>
	 *     Use the method {@link #setEvidenceNode(int)} to set the variable that is to be considered the summarization
	 *     of the evidence (-1 if no evidence).
	 * <p>
	 *
	 * @param model the inference model
	 * @param query the variable whose intervals we are interested in
	 * @return
	 */
	// TODO must support multiple evidence here and in the variable elimination (see {@link ch.idsia.crema.inference.approxlp2.ApproxLP2})
	// TODO: should we binarize the evidence?
	@Override
	public IntervalFactor query(GraphicalModel<F> model, int query) {
		int states = model.getSize(query);

		double[] lowers = new double[states];
		double[] uppers = new double[states];

		for (int state = 0; state < states; ++state) {
			Manager lower;
			Manager upper;

			if (evidenceNode == -1) {
				// without evidence we are looking for a marginal
				EPS = 1e-9;
				lower = new Marginal(model, GoalType.MINIMIZE, query, state);
				upper = new Marginal(model, GoalType.MAXIMIZE, query, state);
			} else {
				EPS = 1e-9;
				lower = new Posterior(model, GoalType.MINIMIZE, query, state, evidenceNode);
				upper = new Posterior(model, GoalType.MAXIMIZE, query, state, evidenceNode);
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

}
