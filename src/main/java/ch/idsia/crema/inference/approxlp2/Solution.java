package ch.idsia.crema.inference.approxlp2;

import ch.idsia.crema.factor.bayesian.BayesianFactor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


public class Solution {

	private final Int2ObjectMap<BayesianFactor> data;
	private final int free;

	private double scorecache;

	/**
	 * Initial solutions have no source
	 *
	 * @param data
	 * @param score
	 */
	Solution(Int2ObjectMap<BayesianFactor> data, double score) {
		this.scorecache = score;
		this.data = data;
		this.free = -1;
	}

	Solution(Solution source, Move move) {
		// shallow copy
		this.data = new Int2ObjectOpenHashMap<>(source.data);

		if (move.getValues() == null)
			throw new IllegalArgumentException("The provided move has never been evaluated");

		this.free = move.getFree(); // info about what just change (usefull for neighbourhood)
		this.data.put(free, move.getValues());
		this.scorecache = move.getScore();
	}

	int getFree() {
		return free;
	}

	/**
	 * Packages accessible method to get the internal data of the solution
	 *
	 * @return
	 */
	Int2ObjectMap<BayesianFactor> getData() {
		return data;
	}

	void setScore(double score) {
		this.scorecache = score;
	}

	public double getScore() {
		return scorecache;
	}
}
