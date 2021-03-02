package ch.idsia.crema.search.impl;

import java.util.List;
import java.util.Random;

public class RandomWalk<M, S> extends AbstractSearch<M, S> {

	private int maxIteration = 1000;
	private final Random generator; // TODO: use crema's random utils


	public RandomWalk() {
		generator = new Random();
	}

	public void setSeed(long seed) {
		generator.setSeed(seed);
	}

	public int getMaxIteration() {
		return maxIteration;
	}

	public void setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
	}

	@Override
	public boolean step() {
		List<M> candidates = neighbourhood.neighbours(currentSolution);
		int winner = generator.nextInt(candidates.size());
		M move = candidates.get(winner);

		currentSolution = neighbourhood.move(currentSolution, move);
		if (objective.compare(currentSolution, bestSolution) < 0) {
			bestSolution = currentSolution;
		}
		return false;
	}
}
