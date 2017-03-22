package ch.idsia.crema.search;

import java.util.Comparator;

public interface ObjectiveFunction<M, S> extends Comparator<S>  {
	/**
	 * Get the objective function value for doing the specified move on the specified solution
	 * 
	 * @param from
	 * @param doing if null from will be evaluated
	 * @return
	 */
	double eval(S from, M doing);
	
	/**
	 * This might and should be mapped to eval(solution, null);
	 * Evaluation of a solution should really be cached. This method might be called multiple times
	 * for a solution
	 * 
	 * @param solution
	 * @return
	 */
	double eval(S solution);
	
	/**
	 * since we did not specify if a positive change is good or bad here we 
	 * can get that information
	 * 
	 * @param change
	 * @return
	 */
	boolean isImprovement(double change);
	
	/**
	 * since we did not specify if a positive change is good or bad here we 
	 * can get that information. 
	 * If any of the inputs is NaN the other must be an improvement. If both are
	 * NaN it should not be an improvement.
	 * 
	 * @param from 
	 * @param to 
	 * @return
	 */
	boolean isImprovement(double from, double to);
	
	
	/**
	 * Test whether the provided score is a bound. 
	 * This will be used to test if we reached a known upper/lower bound and should not continue.
	 * @param value a score
	 * @return true if the score is a bound in the optimizing direction
	 */
	boolean isBound(double value);
}
