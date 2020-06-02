package ch.idsia.crema.search;

import java.util.List;

public interface NeighbourhoodFunction<M, S> {
	/**
	 * Provide the caller with the possible moves available when in the specified Solution.
	 * 
	 * @param solution 
	 * @return A list of moves
	 */
	List<M> neighbours(S solution);
	
	/**
	 * Generate a random solution. This can be used for generating a starting point for some
	 * algorithms.
	 * 
	 * @return
	 */
	S random();
	
	/**
	 * Perform the specified move for the `from` solution. The resulting solution is returned
	 * 
	 * @param from
	 * @param doing
	 * @return
	 */
	S move(S from, M doing);
}
