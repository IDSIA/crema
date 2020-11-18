package ch.idsia.crema.model.graphical.specialized;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.GenericGraphicalModel;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 10:57
 * <p>
 * A BayesianNetwork is a special type of {@link GenericGraphicalModel}, composed with {@link BayesianFactor} and
 * constructed on a {@link SparseDirectedAcyclicGraph}.
 */
public class BayesianNetwork extends GenericGraphicalModel<BayesianFactor, SparseDirectedAcyclicGraph> {

	/**
	 * Create the directed model using the specified network implementation.
	 */
	public BayesianNetwork() {
		super(new SparseDirectedAcyclicGraph());
	}

	public TIntIntMap[] samples(int N, int... vars) {
		return IntStream.range(0, N).mapToObj(i -> sample(vars)).toArray(TIntIntMap[]::new);
	}

	public TIntIntMap sample(int... vars) {
		TIntIntMap obs = new TIntIntHashMap();

		for (Integer v : this.getNetwork()) {
			BayesianFactor f = this.getFactor(v).copy();
			for (int pa : this.getParents(v)) {
				f = f.filter(pa, obs.get(pa));
			}
			obs.putAll(f.sample());
		}

		if (vars.length == 0)
			vars = this.getVariables();

		for (int v : obs.keys())
			if (!ArraysUtil.contains(v, vars))
				obs.remove(v);

		return obs;
	}

	public double[] logProb(TIntIntMap[] data) {
		return IntStream.of(this.getVariables()).mapToDouble(v -> this.getFactor(v).logProb(data, v)).toArray();
	}

	public double sumLogProb(TIntIntMap[] data) {
		return DoubleStream.of(this.logProb(data)).sum();
	}

}
