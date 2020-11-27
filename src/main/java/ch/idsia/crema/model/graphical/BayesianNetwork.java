package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.Inference;
import gnu.trove.map.TIntIntMap;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 10:57
 * <p>
 * A BayesianNetwork is a special type of {@link DAGModel}, composed with {@link BayesianFactor} and
 * constructed on a {@link BayesianNetwork}.
 */
public class BayesianNetwork extends DAGModel<BayesianFactor> {

	public double[] logProb(TIntIntMap[] data) {
		return IntStream.of(this.getVariables()).mapToDouble(v -> this.getFactor(v).logProb(data, v)).toArray();
	}

	public double sumLogProb(TIntIntMap[] data) {
		return DoubleStream.of(this.logProb(data)).sum();
	}

}
