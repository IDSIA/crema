package ch.idsia.crema.learning;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <F>
 * @author Claudio Bonesana
 */
public abstract class EMAlgorithm<F extends OperableFactor<F>> {

	protected final InferenceJoined<GraphicalModel<F>, F> inference;

	private double scoreThreshold = 0.0;
	private long iterations;
	private List<Double> scores;

	/**
	 * @param inference engine or algorithm to use for the inferences
	 */
	public EMAlgorithm(InferenceJoined<GraphicalModel<F>, F> inference) {
		this.inference = inference;
	}

	/**
	 * Set the threshold to use to determine the convergence of the algorithm. The algorithm stops when the difference
	 * between the score achieved in the previous step and the current one is smaller than the assigned threshold. In
	 * other words, the algorithm stops when there is no more improvement or the improvement is not significant.
	 *
	 * @param scoreThreshold threshold to use. Default value: 0.0
	 * @return chained object
	 */
	public EMAlgorithm<F> setScoreThreshold(double scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
		return this;
	}

	/**
	 * @return the number of iterations done using the {@link #run(GraphicalModel, TIntIntMap[])} method.
	 */
	public long getIterations() {
		return iterations;
	}

	/**
	 * @return a list of scores collected during {@link #run(GraphicalModel, TIntIntMap[])} method execution. First
	 * value is the score for the input model. Score of first iteration is at index 1.
	 */
	public List<Double> getScores() {
		return scores;
	}

	/**
	 * Expectation step. This step will produce a map of counts in the form of factors. This map will be maximized by
	 * the next step.
	 *
	 * @param model        starting model, its structure will be used to learn from the observations
	 * @param observations data to learn from
	 * @return a map of variable index - factors where these are the counts for each factor collected from the data
	 */
	public abstract TIntObjectMap<F> expectation(GraphicalModel<F> model, TIntIntMap[] observations);

	/**
	 * Maximization step. Elaborate the input factor map by producing and assigning the factors to a copy of the input
	 * model.
	 *
	 * @param model   starting model, its structure will be copied
	 * @param factors map of counts in factor format
	 * @return the model with the factors learned from the data
	 */
	public abstract GraphicalModel<F> maximization(GraphicalModel<F> model, TIntObjectMap<F> factors);

	/**
	 * Score to evaluate the convergence of the algorithm. Most of the time, this is a likelihood ora log-likelihood.
	 * <p>
	 * The convergence is established by the difference in improvement (difference in score) between the previous step
	 * and the current step of the algorithm. The {@link #scoreThreshold} is used to establish if the improvement in the
	 * score is enough (and then stop the algorithm) or not.
	 *
	 * @param model        model learned during a {@link #step(GraphicalModel, TIntIntMap[])} operation
	 * @param observations data to learn from
	 * @return a score that establish the
	 */
	public abstract double score(GraphicalModel<F> model, TIntIntMap[] observations);

	/**
	 * Performs one single step of maximization followed by one step of expectation.
	 *
	 * @param model        starting model, its structure will be copied
	 * @param observations data to learn from
	 * @return a model with the input structure but with updated factors
	 */
	public GraphicalModel<F> step(GraphicalModel<F> model, TIntIntMap[] observations) {

		TIntObjectMap<F> factors = expectation(model, observations);
		GraphicalModel<F> newModel = maximization(model, factors);

		return newModel;
	}

	/**
	 * Perform the Expectation-Maximization algorithm until convergence. The algorithm continues to run until the
	 * improvement of the score, respect to the previous step, is greater than the {@link #scoreThreshold} parameter.
	 *
	 * @param model        starting model, its structure will be copied
	 * @param observations data to learn from
	 * @return the learned model from the given data
	 */
	public GraphicalModel<F> run(GraphicalModel<F> model, TIntIntMap[] observations) {
		double lastModelScore;
		double currentModelScore = score(model, observations);
		iterations = 0;
		scores = new ArrayList<>();
		scores.add(currentModelScore);

		do {
			iterations++;
			lastModelScore = currentModelScore;
			model = step(model, observations);
			currentModelScore = score(model, observations);
			scores.add(currentModelScore);
		} while (currentModelScore - lastModelScore > scoreThreshold);

		return model;
	}

}
