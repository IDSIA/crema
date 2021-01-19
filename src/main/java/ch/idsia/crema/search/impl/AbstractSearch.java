package ch.idsia.crema.search.impl;

import ch.idsia.crema.search.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A common implementation of a search algorith offering observers, progress and stats.
 * The class also implement the run as a consecutive series of calls to step (which is abstract).
 *
 * <p>Contributions to the progress are possible overriding the {@link #getProgress()} method. This will
 * return a value [0-1] indicating any other limit's progress. This value is maxed with the time and
 * iteration progress.</p>
 *
 * @param <M>
 * @param <S>
 * @author david
 */
public abstract class AbstractSearch<M, S> implements ISearch<M, S>, IStatCapable<M> {
	protected ObjectiveFunction<M, S> objective;
	protected NeighbourhoodFunction<M, S> neighbourhood;

	protected S bestSolution;
	protected double bestScore = Double.NaN;

	protected double currentScore;
	protected S currentSolution;

	protected long maxIterations = Long.MAX_VALUE;
	protected long maxDuration = -1;

	protected IStats<M> stats;
	protected long iteration;

	protected AtomicBoolean stopFlag;

	private double progress;
	private int steps = 10;

	private LinkedList<ProgressListener> listeners;

	public AbstractSearch() {
		listeners = new LinkedList<>();
	}

	@Override
	public void addProgressListener(ProgressListener listener) {
		listeners.add(listener);
	}

	public void removeProgressListener(ProgressListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setObjectiveFunction(ObjectiveFunction<M, S> obj) {
		this.objective = obj;
	}

	@Override
	public void setNeighbourhoodFunction(NeighbourhoodFunction<M, S> obj) {
		this.neighbourhood = obj;
	}

	@Override
	public S best() {
		return bestSolution;
	}

	/**
	 * @param initial the initial solution
	 * @precondition the objective function must have been set
	 */
	@Override
	public void initialize(S initial, Map<String, Object> config) {

		this.bestSolution = this.currentSolution = initial;
		bestScore = currentScore = objective.eval(currentSolution);


		if (config != null && config.containsKey(MAX_ITER)) {
			maxIterations = Utils.tryParse(config.get(MAX_ITER), Long.MAX_VALUE);
		}

		if (config != null && config.containsKey(MAX_TIME)) {
			double maxDuration = Utils.tryParse(config.get(MAX_TIME), -1.0);
			if (maxDuration >= 0) this.maxDuration = (long) maxDuration * 1000; // we get seconds but need milliseconds
		}

		// no external flag set
		if (stopFlag == null) stopFlag = new AtomicBoolean(false);
	}

	/**
	 * Performs a single step in the search. The method returns true if a move has been performed. When the method returns false
	 * one can expect that any further call to step will not move the searcher from the current solution. This is particularly
	 * useful to interrupt a loop that calls step:
	 *
	 * <pre><code>
	 * while(search.step());
	 * </code></pre>
	 * <p>
	 * An implementation of this interface could also include an iteration counter and set an iteration limit. The implementation must
	 * then also check that if the maximum number of iterations has been reached a call to step will just return false and not
	 * perform a move.
	 * <p>
	 * <p/>Moves do not necessarily have to improve the best solution.
	 *
	 * @return true when a move has been done.
	 */
	protected abstract boolean step();

	@Override
	public void setStop(AtomicBoolean stop) {
		stopFlag = stop;
	}

	@Override
	public double run() throws InterruptedException {
		iteration = 0;
		long endTime = maxDuration > 0 ? System.currentTimeMillis() + maxDuration : Long.MAX_VALUE;

		this.progress = 0;

		while (step() && !objective.isBound(bestScore)) {
			if (stopFlag.get()) throw new InterruptedException();

			if (iteration++ > maxIterations)
				break;

			long now = System.currentTimeMillis();

			if (endTime < now)
				break;

			progressUpdate(1.0 - (endTime - now) / (double) maxDuration, iteration / (double) maxIterations);
		}

		// make sure the event is sent out
		this.progress = 0;
		progressUpdate(1, 1);

		return bestScore;
	}

	private void progressUpdate(double timeFraction, double iterationFraction) {
		double progress = Math.max(timeFraction, iterationFraction);
		progress = Math.max(progress, getProgress()); // get more progress from subclasses

		if (progress >= this.progress + 1.0 / steps) {
			this.progress = progress;
			ProgressEvent event = new ProgressEvent(this, progress);
			for (ProgressListener listener : listeners) {
				listener.progressed(event);
			}
		}

	}

	@Override
	public void setStats(IStats<M> stats) {
		this.stats = stats;
	}

	@Override
	public IStats<M> getStats() {
		return stats;
	}

	protected double getProgress() {
		return 0.0;
	}

}
