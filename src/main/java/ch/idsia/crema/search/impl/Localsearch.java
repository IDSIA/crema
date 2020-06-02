package ch.idsia.crema.search.impl;

import java.util.List;

public class Localsearch<M, S> extends AbstractSearch<M, S> {

	@Override
	protected boolean step() {
		// System.out.println("New step");
		List<M> moves = neighbourhood.neighbours(currentSolution);

		double winner_score = currentScore;
		M winner_move = null;

		for (M move : moves) {
			double score = objective.eval(currentSolution, move);
			if (objective.isImprovement(winner_score, score)) {
				// System.out.println("Winner improved: " + move);
				winner_move = move;
				winner_score = score;
				// break; // find best
			}
		}

		// no improving solution
		if (winner_move == null) {
			// there was no possible improvement
			// System.out.println("No improvement");
			// currentSolution = neighbourhood.random();
			return false;
		}

		currentSolution = neighbourhood.move(currentSolution, winner_move);
		currentScore = winner_score;
		if (stats != null)
			stats.move(winner_move);

		if (objective.isImprovement(bestScore, currentScore)) {
			bestSolution = currentSolution;
			bestScore = currentScore;
			if (stats != null)
				stats.improvement(iteration, bestScore);
		}
		return true;
	}

}
