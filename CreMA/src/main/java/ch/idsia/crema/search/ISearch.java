package ch.idsia.crema.search;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ISearch<M, S> {
	
	public static String MAX_TIME = "maxTime";
	
	public static String MAX_ITER = "maxIterations";
	
	/**
	 * Add a listener that will be informed about progresses done by the searcher. There is no guarantee that any event is emitted except by the ending event.
	 * TODO use this also to notify improvements about the actual results improvements.
	 * @param listener
	 */
	void addProgressListener(ProgressListener listener);
	
	/**
	 * Set the atomic boolean to be used to notify the searcher that we want to stop.
	 * TODO change in stop() and push the event from the Algorithm into the searcher.
	 * @param stop
	 */
	void setStop(AtomicBoolean stop);
	
	/**
	 * This method tells the search algorithm about the objective function to be used to evaluate a move on a solution.
	 * Must be called before the initialization or the {@link ISearch.initialize} might fail.
	 * 
	 * @param obj the objective function
	 */
	void setObjectiveFunction(ObjectiveFunction<M, S> obj);

	/**
	 * This method set the neighbourhood function that the search algorithm should use. Some more complex search algorithms might integrate the 
	 * neighbourhood into themselves ignoring this method. 
	 * 
	 * TODO is this the case? should we have a way to tell the caller?
	 * @param obj
	 */
	void setNeighbourhoodFunction(NeighbourhoodFunction<M, S> obj);
	
	/**
	 * Prepare the search with the specified starting solution and search conditions.
	 * @param initial
	 */
	void initialize(S initial, Map<String, Object> init);

	
	/**
	 * Returns the best solution found up to this moment. If {@link ISearch.step} did not return false the best solution
	 * might change after a further call to step. On the other hand when step did return false, best must return the same
	 * solution all the time. Changes that will make step continue the search (increase in max iterations)  
	 * 
	 * @return
	 */
	S best();
	
	/**
	 * keep calling step until return value is true or some other halting criteria is met.
	 */
	double run() throws InterruptedException;
}
