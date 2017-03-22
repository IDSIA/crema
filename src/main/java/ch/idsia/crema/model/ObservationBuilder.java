package ch.idsia.crema.model;

import gnu.trove.map.hash.TIntIntHashMap;

public class ObservationBuilder extends TIntIntHashMap {
	private int vars[];
	
	public static ObservationBuilder observe(int var, int state) {
		return new ObservationBuilder(new int[] { var }, new int[] { state });
	}

	public ObservationBuilder and(int var, int state) {
		put(var, state);
		return this;
	}
	
	public static ObservationBuilder vars(int... vars) {
		return new ObservationBuilder(vars);
	}

	public ObservationBuilder states(int... states) {
		putAll(new TIntIntHashMap(vars, states));
		return this;
	}
	
	private ObservationBuilder(int[] keys) {
		super(keys.length);
		vars = keys;
	}
	
	private ObservationBuilder(int[] keys, int[] values) {
		super(keys, values);
	}

}
