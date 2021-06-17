package ch.idsia.crema.search.impl;

import ch.idsia.crema.utility.RandomUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @param <M> the move type
 * @param <S> the solution type
 * @author davidhuber
 */
public class GreedyWithRandomRestart<M, S> extends AbstractSearch<M, S> {
	public static final String MAX_RESTARTS = "maxrestarts";
	public static final String MAX_PLATEAU = "maxplateau";

	public static final int MAX_RESTARTS_DEFAULT = 5;
	public static final int MAX_PLATEAU_DEFAULT = 3;

	protected Random random = RandomUtil.getRandom();

	private int maxRestarts = MAX_RESTARTS_DEFAULT;
	private int restarts = MAX_RESTARTS_DEFAULT; // counting down

	private int maxPlateau = MAX_PLATEAU_DEFAULT;
	private int plateau = 0; // counting up 

	@Override
	public void initialize(S initial, Map<String, Object> config) {
		maxRestarts = MAX_RESTARTS_DEFAULT;
		maxPlateau = MAX_PLATEAU_DEFAULT;
		plateau = 0;

		if (config.containsKey(MAX_RESTARTS)) {
			maxRestarts = Utils.tryParse(config.get(MAX_RESTARTS), MAX_RESTARTS_DEFAULT);
		}

		if (config.containsKey(MAX_PLATEAU)) {
			maxPlateau = Utils.tryParse(config.get(MAX_PLATEAU), MAX_PLATEAU_DEFAULT);
		}

		super.initialize(initial, config);

		restarts = maxRestarts;
		plateau = 0;
	}

	@Override
	public boolean step() {
		List<M> moves = neighbourhood.neighbours(currentSolution);
		Collections.shuffle(moves, random);

		double winner_score = currentScore;
		M winner_move = null;
		M last_valid = null;

		for (M move : moves) {
			double score = objective.eval(currentSolution, move);
			if (objective.isImprovement(winner_score, score)) {
				winner_move = move;
				winner_score = score;
				// break;
			} else if (!Double.isNaN(score) && !Double.isInfinite(score)) {
				// assuming we can only improve or be on a plateau any not winning valid move 
				// is a plateau move!
				last_valid = move;
			}
		}

		// no improving move found
		if (winner_move == null) {

			// max plateau reached or no other valid move available
			if (plateau >= maxPlateau || last_valid == null) {
				currentSolution = neighbourhood.random();
				currentScore = Double.NaN;
				if (stats != null)
					stats.reset(iteration);

				plateau = 0;
				return --restarts > 0;
			} else { // perform the last changing move
				++plateau;
				winner_move = last_valid;
			}
		} else {
			// found improving move, clear plateau steps
			plateau = 0;
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
