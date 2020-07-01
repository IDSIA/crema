package ch.idsia.crema.model;

import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.hash.TIntIntHashMap;

public class ObservationBuilder extends TIntIntHashMap {
	private int vars[];
	
	public static ObservationBuilder observe(int var, int state) {
		return new ObservationBuilder(new int[] { var }, new int[] { state });
	}

	public static ObservationBuilder observe(int[] vars, int[] states) {
		return new ObservationBuilder(vars, states);
	}

	public static ObservationBuilder[] observe(int[] vars, int[][] data) {
		ObservationBuilder[] observaitons = new ObservationBuilder[data.length];
		for(int i=0; i<data.length; i++) {

			int[] data_i = ArraysUtil.slice(data[i], ArraysUtil.where(data[i], x->x>=0));
			int[] vars_i = ArraysUtil.slice(vars, ArraysUtil.where(data[i], x->x>=0));
			observaitons[i] = ObservationBuilder.observe(vars_i, data_i);
		}

		return observaitons;
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
