package ch.idsia.crema.core;

import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ObservationBuilder extends TIntIntHashMap {

	private int[] vars;

	public static ObservationBuilder observe(int var, int state) {
		return new ObservationBuilder(new int[]{var}, new int[]{state});
	}

	public static ObservationBuilder observe(int[] vars, int[] states) {
		return new ObservationBuilder(vars, states);
	}

	public static ObservationBuilder[] observe(int[] vars, int[][] data) {
		ObservationBuilder[] observaitons = new ObservationBuilder[data.length];
		for (int i = 0; i < data.length; i++) {

			int[] data_i = ArraysUtil.slice(data[i], ArraysUtil.where(data[i], x -> x >= 0));
			int[] vars_i = ArraysUtil.slice(vars, ArraysUtil.where(data[i], x -> x >= 0));
			observaitons[i] = ObservationBuilder.observe(vars_i, data_i);
		}

		return observaitons;
	}

	public static ObservationBuilder[] observe(String[] vars, double[][] data) {
		return observe(Stream.of(vars).mapToInt(Integer::valueOf).toArray(), data);
	}

	public static ObservationBuilder[] observe(int[] vars, double[][] data) {
		ObservationBuilder[] obs = new ObservationBuilder[data.length];
		for (int i = 0; i < data.length; i++) {
			int[] valid = ArraysUtil.where(data[i], x -> !Double.isNaN(x));
			obs[i] = ObservationBuilder.observe(
					ArraysUtil.slice(vars, valid),
					DoubleStream.of(ArraysUtil.slice(data[i], valid)).mapToInt(v -> (int) v).toArray()
			);
		}
		return obs;
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

	public static int[] getVariables(TIntIntMap[] obs) {
		int[] variables = new int[]{};
		for (TIntIntMap o : obs)
			variables = ArraysUtil.union_unsorted_set(variables, o.keys());
		return variables;
	}

	public static double[][] toDoubles(TIntIntMap[] obs, int... variables) {
		if (variables.length == 0)
			variables = getVariables(obs);
		double[][] dataOut = new double[obs.length][variables.length];
		for (int i = 0; i < obs.length; i++) {
			for (int j = 0; j < variables.length; j++) {
				if (obs[i].containsKey(variables[j]))
					dataOut[i][j] = obs[i].get(variables[j]);
				else
					dataOut[i][j] = Double.NaN;
			}
		}
		return dataOut;
	}

	public static ObservationBuilder[] filter(TIntIntMap[] obs, int... variables) {
		return ObservationBuilder.observe(variables, ObservationBuilder.toDoubles(obs, variables));
	}

	public static TIntIntMap[] filter(TIntIntMap[] data, int[] keys, int[] vals) {
		return Stream.of(data)
				.filter(d -> Arrays.equals(IntStream.of(keys).map(d::get).toArray(), vals))
				.toArray(TIntIntMap[]::new);
	}

}
